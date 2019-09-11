#include "sdt.h"

#include <wx/wfstream.h>
#include <wx/gifdecod.h>

#ifdef __WXMAC__
#include <sys/time.h>  // for struct timeval
#include <unistd.h>    // for select()
#include <ApplicationServices/ApplicationServices.h>
#endif // __WXMAC__

#ifdef WIN32
#include <fcntl.h>
#include <io.h>
#endif // WIN32

#define SDT_VERSION "1.01g"
#define INITIAL_WIDTH 600
#define INITIAL_HEIGHT 600


SdtMapListener::SdtMapListener()
{
}

SdtMapListener::~SdtMapListener()
{
}

SdtFrameListener::SdtFrameListener()
{
}

SdtFrameListener::~SdtFrameListener()
{
}

////////////////////////////////////////////////////////
// SdtApp implementation

IMPLEMENT_APP(SdtApp)
	
BEGIN_EVENT_TABLE(SdtApp, wxProtoApp)
  EVT_MENU (SdtApp::ID_QUIT,   SdtApp::OnQuit)  
  EVT_MENU (SdtApp::ID_FILEOPEN, SdtApp::OnFileOpen)
  EVT_MENU (SdtApp::ID_ABOUT,  SdtApp::OnAbout)
  EVT_MENU (SdtApp::ID_AUTO,   SdtApp::OnAuto)
  EVT_MENU (SdtApp::ID_FILL,   SdtApp::OnFill)
  EVT_MENU (SdtApp::ID_SCRCAP, SdtApp::OnScreenCap)
  EVT_MENU (SdtApp::ID_LABELS, SdtApp::OnLabels)
  EVT_MENU (SdtApp::ID_MLINKS, SdtApp::OnMlinks)
  EVT_BUTTON (wxID_OK, SdtApp::OnButtonOK)
END_EVENT_TABLE()


SdtApp::SdtApp()
: map_frame(NULL), map_canvas(NULL), current_sprite(NULL),pipe_current_sprite(NULL),
  current_node(NULL),pipe_current_node(NULL),
  current_popup(NULL), bg_bitmap(NULL), bg_imagewidth(0), bg_imageheight(0), 
  seeking_cmd(true),pipe_seeking_cmd(true), current_input(ProtoDispatcher::INVALID_DESCRIPTOR), 
  input_wait(0), 
  input_index(0), quoting(false), control_pipe(ProtoPipe::MESSAGE), current_link(NULL),
  comment_in_effect(false), toggle_on(false), mlinks_on(true), link_in_effect(false),
  current_src(NULL), current_dst(NULL), current_directedness(NO_ALLS)
#ifdef WIN32
   ,console_input(false)
#endif // WIN32
{  
	strcpy(pipe_name,"sdt");
  //	OpenDebugLog("sdt.log");  // with no log file PLOG's will go to stderr
    update_timer.SetListener(this, &SdtApp::OnUpdate);
    update_timer.SetInterval(0.100);  // 10 times a second for our "animated" gifs
    update_timer.SetRepeat(-1);
    
    control_pipe.SetNotifier(&GetSocketNotifier());
    control_pipe.SetListener(this, &SdtApp::OnControlMsg);
    wait_timer.SetListener(this, &SdtApp::OnWaitTimeout);
	script_path[0] = '\0';  // initialize to no path
	current_link_ID[0] = '\0';
}

SdtApp::~SdtApp()
{
}

void SdtApp::OnQuit(wxCommandEvent& /*event*/)
{
    //if (map_frame) map_frame->Close(true);
	//fprintf(stderr, "SdtApp::OnQuit - Destroy-ing popup_list, if present\n");
	popup_list.Destroy();
	delete map_canvas;
	map_canvas = NULL;  // prevents Update from using a stale ptr
    if (map_frame) map_frame->Destroy();
    running = false;   
}  // end SdtApp::OnQuit()

void SdtApp::OnAbout( wxCommandEvent &WXUNUSED(event) )
{
  (void)wxMessageBox(_T("Scripted Display Tool (sdt)\n")
                      _T("Version ") _T(SDT_VERSION)
                     _T("\nR. Brian Adamson\nRon Lee\nJeff Weston\nNaval Research Laboratory"),
                     _T("About sdt"), wxICON_INFORMATION | wxOK );
}  // end SdtApp::OnAbout()

void SdtApp::OnFileOpen(wxCommandEvent& WXUNUSED(event))
{
	wxString inputFile(_T(""));
	// for wxWidgets < version 2.9.x will need to use wxOPEN
	// below instead of wxFD_OPEN
	inputFile = wxFileSelector(_T("Find Script File"), _T(""),
	                           _T(""), _T("sdt"), _T("*.*"),
	                           wxFD_OPEN);
	//PLOG(PL_ERROR, "SdtApp::OnFileOpen - saving file %s\n",
	  //   (const char*)inputFile.mb_str());
	SetInput(inputFile.mb_str());
}

void SdtApp::OnButtonOK(wxCommandEvent& WXUNUSED(event))
{
	// at present this event handler is only used for reporting
	//  multiple instances of sdt using the same pipe name

	if (current_popup)
	{
		popup_list.Remove(current_popup);
		delete current_popup;
	}

	if (map_canvas)
	{
		delete map_canvas;
		map_canvas = NULL;  // prevents use of stale ptr in Update
	}

	if (map_frame)
		map_frame->Destroy();
}

void SdtApp::OnLabels(wxCommandEvent& WXUNUSED(event))
{
	// toggle all link labels which have been turned on or off

	SdtLink* nextLink = link_list.Head();
	while (nextLink)
	{
		if (nextLink->LinkLabelState() != SdtLink::LABEL_CLEAR)
		{
			if (toggle_on)
				nextLink->SetLinkLabelOn();
			else
				nextLink->SetLinkLabelOff();
		}

		nextLink = nextLink->Next();
	}

	toggle_on = !toggle_on;  // switch for next time
}

void SdtApp::OnMlinks(wxCommandEvent& WXUNUSED(event))
{
	mlinks_on = !mlinks_on;
}

void SdtApp::Usage()
{
    fprintf(stderr, "sdt version %s\n", SDT_VERSION);
    fprintf(stderr, "Usage: sdt <commands> ...\n");
}  // end MbApp::Usage()

const char* const SdtApp::CMD_LIST[] =
{
  "+path",
  "+instance",
  "+bgimage",      // set background image
  "+bgbounds",
  "+bgsize",
  "+bgscale",
  "+sprite",
  "+image",
  "+size",
  "+scale",
  "+status",
  "+node",
  "+type",
  "+position",
  "+label",
  "+symbol",
  "+delete",  // remove node and any links to it
  "+clear",   // remove all nodes and links
  "+link",
  "+linklabel",
  "+unlink",
  "+line",
  "+wait",
  "+input",
  "-console",
  "+popup",
  "+popdown",
  "+content",
  "-resize",  // resize popup window
  "+title",
  "+log",
  "+region",   // start of sdt 3d commands (ignored for now)
  "+shape",
  "+center",
  "+tile",
  "+tileImage",
  "+sector",
  "+defaultAltitudeType",
  "+flyto",
  "+follow",
  "+listen",
  NULL
};

SdtApp::CmdType SdtApp::GetCmdType(const char* cmd)
{
    if (!cmd) return CMD_INVALID;
    unsigned int len = strlen(cmd);
    bool matched = false;
    CmdType type = CMD_INVALID;
    const char* const* nextCmd = CMD_LIST;

	// test for non-abbreviate-able command(s)
	if (!strcmp(cmd, "link") || !strcmp(cmd, "tile"))
		return CMD_ARG;

    while (*nextCmd)
    {
        if (!strncmp(cmd, *nextCmd+1, len))
        {

            if (matched)
            {
                // ambiguous command (command should match only once)
                fprintf(stderr, "SdtApp::GetCmdType - ambiguous cmd (%s) for (%s)\n", cmd, *nextCmd+1);
                return CMD_INVALID;
            }
            else
            {
                matched = true; 
                if ('+' == *nextCmd[0])
                    type = CMD_ARG;
                else
                    type = CMD_NOARG;
            }
        }
        nextCmd++;
    }
    return type;
}  // end SdtApp::GetCmdType()   

bool SdtApp::OnCommand(const char* cmd, const char* val)
{
    //fprintf(stderr, "SdtApp::OnCommand() cmd>%s val>%s\n",cmd,val);


    //PLOG(PL_ERROR,"Cmd>%s val>%s\n",cmd,val);
    unsigned int len = strlen(cmd);
    if (!strncmp(cmd, "bgimage", len))
    {
        return SetBackgroundImage(val);
    }
    else if (!strncmp(cmd,"path",len))
    {
        return SetPath(val);
    }
	else if (!strncmp(cmd,"instance",len))
	{
		PLOG(PL_ERROR,"Setting instance %s\n",val);
		return SetPipe(val);
	}
    else if (!strncmp(cmd, "bgsize", len))
    {
        long width, height;
        if (2 != sscanf(val, "%lu,%lu", &width, &height))
        {
            fprintf(stderr, "SdtApp::OnCommand() invalid bgsize\n");
            return false;   
        }
        return SetBackgroundSize(width, height);
    }
    else if (!strncmp(cmd, "bgscale", len))
    {
        double factor;
        if (1 != sscanf(val, "%lf", &factor))
        {
            fprintf(stderr, "SdtApp::OnCommand() invalid bgscale factor\n");
            return false;   
        }
        if (!ScaleBackground(factor)) return false;
    }
    else if (!strncmp(cmd, "bgbounds", len))
    {
        double top, left, bottom, right;
        if (4 != sscanf(val, "%lf,%lf,%lf,%lf", 
                        &left, &top, &right, &bottom))
        {
            fprintf(stderr, "SdtApp::OnCommand() invalid bgbounds\n");
            return false;   
        }
        SetBounds(left, top, right, bottom);
    }
    else if (!strncmp(cmd, "sprite", len))
    {
        return SetSprite(val);
    }
    else if (!strncmp(cmd, "image", len))
    {
        return SetSpriteImage(val);
    }
    else if (!strncmp(cmd, "size", len))
    {
        long width, height;
        if (2 != sscanf(val, "%lu,%lu", &width, &height))
        {
            fprintf(stderr, "SdtApp::OnCommand() invalid sprite size\n");
            return false;   
        }
        if (!SetSpriteSize(width, height)) return false;
    }
    else if (!strncmp(cmd, "scale", len))
    {
        double factor;
        if (1 != sscanf(val, "%lf", &factor))
        {
            fprintf(stderr, "SdtApp::OnCommand() invalid sprite scale factor\n");
            return false;   
        }
        return ScaleSprite(factor);
    }
    else if (!strncmp(cmd, "status", len))
    {
        if (!SetStatus(val)) return false;
    }
    else if (!strncmp(cmd, "node", len))
    {
        //const char* embeddedSemi = strchr(val, ';');
		//if (embeddedSemi)
		//{
		//	fprintf(stderr, "OnCommand - found semicolon in node name\n");
		//	return false;
		//}

        if (!SetNode(val)) return false;  // o/w fall through
    }
    else if (!strncmp(cmd, "type", len))
    {
        return SetNodeType(val);
    }
    else if (!strncmp(cmd, "position", len))
    {
		char valCopy[CMD_LINE_MAX];
		strcpy(valCopy, val);  // TBD - limit-check
		char* xpos = strtok(valCopy, ",");
		char* ypos = strtok(NULL, ",");
		char* zpos = strtok(NULL, ",");  // not presently used
		char* aglMsl = strtok(NULL, ",");  // not presently used
		if (aglMsl != NULL)
			fprintf(stderr, "OnCommand (position) - zpos = %s, aglMsl = %s\n", zpos, aglMsl);

		return SetNodePosition(xpos, ypos);
	}
    else if (!strncmp(cmd, "label", len))
    {
		return SetNodeLabel(val);  // off|on|<color>[,text]
    }
    else if (!strncmp(cmd, "symbol", len))
    {
        // symbol <symbolType>[,<color>[,<thickness>[,<x_radius>[,<y_radius>[,<opacity>]]]]]
		char valCopy[CMD_LINE_MAX];
		strcpy(valCopy, val);  // TBD - limit-check

		char* symbolType = strtok(valCopy, ",");
		char* symbolColor = strtok(NULL, ",");  // NULL if not present
		char* symbolThickness = strtok(NULL, ",");
		char* symbolXradius = strtok(NULL, ",");
		char* symbolYradius = strtok(NULL, ",");
		if (symbolYradius == NULL)
			symbolYradius = symbolXradius;  // take x value if null
		char* symbolOpacity = strtok(NULL, ",");  // not presently used
		if (symbolOpacity != NULL)
			fprintf(stderr, "OnCommand (symbol) - opacity = %f\n", atof(symbolOpacity));

		if (current_node == NULL)
		{
			fprintf(stderr, "OnCommand (symbol) - no current node defined\n");
			return false;
		}

		bool typeOK = current_node->SetSymbol(symbolType);
		bool colorOK = current_node->SetSymbolColor(symbolColor);
		bool widthOK = current_node->SetSymbolThickness(symbolThickness);
		bool xradOK = current_node->SetCircleXRadius(symbolXradius);
		bool yradOK = current_node->SetCircleYRadius(symbolYradius);

		return (typeOK && colorOK && widthOK && xradOK && yradOK);
	}
    else if (!strncmp(cmd, "delete", len))
    {
		char valCopy[CMD_LINE_MAX];
		strcpy(valCopy, val);  // assumes val is already NULL checked

		char* objectType = strtok(valCopy, ",");
		char* objectName = strtok(NULL, ",");

		if (!objectType)
			return false;

		// delete {node|link|region|tile},<objectName>
		if (!strcmp(objectType, "node"))
			return DeleteNode(objectName);
		if (!strcmp(objectType, "link"))
		{
			char dcCopy[CMD_LINE_MAX];
			strcpy(dcCopy, val);

			char* linkSrc = objectName;  // transfer over
			char* linkDst = strtok(NULL, ",");
			char* linkIDorAll = strtok(NULL, ",");
			char* dirOrAll = strtok(NULL, ",");
			// <node1>,<node2>[,<linkID>[,dir|all]]
			if (HasDblCommaLinkID(dcCopy))
			{
				dirOrAll = linkIDorAll;  // must shift back to right
				linkIDorAll = NULL;  // what double comma really meant
			}
			return RemoveLink(linkSrc, linkDst, linkIDorAll, dirOrAll);
		}
		if (!strcmp(objectType, "region"))
			return true;  // not yet implemented
		if (!strcmp(objectType, "tile"))
			return true;  // not yet implemented

		// (deprecated) delete <nodeName>
		return DeleteNode(objectType);  // no "node" as type
    }
    else if (!strncmp(cmd, "clear", len))
    {
		if (!strcmp(val, "all") || !strcmp(val, "nodes"))
		{
			link_list.Destroy();  // also removes linklabels
			node_list.Destroy();  // removes sprites, symbols
		}
		else if (!strcmp(val, "sprites"))
		{
			// remove sprites from nodes, leave sprite list alone
			node_list.RemoveSprites();
		}
		else if (!strcmp(val, "symbols"))
			node_list.RemoveSymbols();  // remove symbols from nodes
		else if (!strcmp(val, "links"))
			link_list.Destroy();
		else if (!strcmp(val, "linklabels"))
			link_list.RemoveLinkLabels(); // remove labels from links
		else if (!strcmp(val, "regions"))
			fprintf(stderr, "SdtApp::OnCommand - \"clear regions\" not yet implemented\n");
		else if (!strcmp(val, "tiles"))
			fprintf(stderr, "SdtApp::OnCommand - \"clear tiles\" not yet implemented\n");
		else
		{
			fprintf(stderr, "SdtApp::OnCommand - invalid clear option (%s)\n", val);
			return false;
		}

		return true;
    }
    else if (!strcmp(cmd, "link"))  // cannot allow abbreviations (see linklabel)
    {
		// link <source>,<dest>[,<color> | <linkID> | all[, dir | all]]
		char valcpy[CMD_LINE_MAX];
		char dcCopy[CMD_LINE_MAX];

		strcpy(valcpy, val);  // TBD - limit-check
		strcpy(dcCopy, val);

		char* src = strtok(valcpy, ",");
		char* dst = strtok(NULL, ",");
		char* colorLinkID = strtok(NULL, ",");  // could also be "all"
		// could be width, "dir", or "all" (new)
		char* widthDirected = strtok(NULL, ",");

		// was a double comma used to specify NULL linkID?
		if (HasDblCommaLinkID(dcCopy))
		{  // if so then adjust linkID and width/dir/all
			widthDirected = colorLinkID;  // strtok skips over the DC
			colorLinkID = NULL;
		}

		if (OldSyntax(colorLinkID, widthDirected))
		{
			// (deprecated) link <source>,<dest>[,<color>[,<thickness>]]
			bool linkOK = AddLink(src, dst, NULL, NULL);
			bool lineOK = SetLine(colorLinkID, widthDirected);
			return (linkOK && lineOK);
		}
		else  // must be new syntax
		{
			// last two args could be NULL
			return AddLink(src, dst, colorLinkID, widthDirected);
		}
	}
	else if (!strncmp(cmd, "linklabel", len))  // min abbreviation is "linkl"
	{
		return SetLinkLabel(val);
	}
	else if (!strncmp(cmd, "unlink", len))
    {
		char valcpy[CMD_LINE_MAX];
		char dcCopy[CMD_LINE_MAX];

		strcpy(valcpy, val);  // TBD - limit check?
		strcpy(dcCopy, val);

		char* src = strtok(valcpy, ",");
        char* dst = strtok(NULL, ",");
		char* linkIDorAll = strtok(NULL, ",");  // could be NULL
		char* dirOrAll = strtok(NULL, ",");
		if (HasDblCommaLinkID(dcCopy))
		{
			dirOrAll = linkIDorAll;  // strtok cannot detect dbl commas,
			linkIDorAll = NULL;      // so have to look for it & adjust
		}
		return RemoveLink(src, dst, linkIDorAll, dirOrAll);  // NULL linkID is OK
    }
	else if (!strncmp(cmd, "line", len))
	{
		char valcpy[CMD_LINE_MAX];
		strcpy(valcpy, val);

		char* color = strtok(valcpy, ",");
		char* width = strtok(NULL, ",");
		// determine line color and thickness
		return SetLine(color, width);
	}
    else if (!strncmp(cmd, "popup", len))
    {
        return SetPopup(val);
    }
    else if (!strncmp(cmd, "popdown", len))
    {
        DeletePopup(val);
    }
    else if (!strncmp(cmd, "content", len))
    {
        return SetPopupContent(val);
    }
    else if (!strncmp(cmd, "resize", len))
    {
        return ResizePopup();
    }
#ifdef WIN32
    else if (!strncmp(cmd, "console", len))
    {
        console_input = true;
        current_input = GetStdHandle(STD_INPUT_HANDLE);
    }
#endif // WIN32
    else if (!strncmp(cmd, "input", len))
    {
        return SetInput(val);
    }
    else if (!strncmp(cmd, "wait", len))
    {
        unsigned long delay;
        if (1 != sscanf(val, "%lu", &delay))
        {
            fprintf(stderr, "SdtApp::OnCommand() invalid delay value\n");
            return false;   
        }
		//fprintf(stderr, "OnCommand - waiting %f secs\n", delay / 1000.0);
        input_wait = delay;
        wait_timer.SetInterval(input_wait/1000);
        wait_timer.SetRepeat(0);
        ActivateTimer(wait_timer);
        //	while(wait_timer.IsActive()){
        //	}
        // Remove Generic Input for wait period.
        // [Note: GenericInput will be restored with OnWaitTimeout()]:
        dispatcher.RemoveGenericInput(current_input);
    } // else if "wait"
    else if (!strncmp(cmd, "title", len))
    {
        // title
        char* valcpy = new char[strlen(val)+1];
        if (!valcpy)
        {
            perror("SdtApp::OnCommand() error no title provided\n");
            return false;   
        }
        strcpy(valcpy, val);
        //map_frame->SetTitle(_T(valcpy));
		if (!map_frame)
			return false;
        map_frame->SetTitle(wxString::FromAscii(valcpy));
	return true;
    }
	else if (!strncmp(cmd, "log", len))
	{
		if (!strcmp(val, "off"))
		{
			CloseDebugLog();
			return true;
		}
		else
			return OpenDebugLog(const_cast<char*>(val));
	}
	else if (!strncmp(cmd, "region", len))  // SDT 3D commands (now ignored)
		return true;  // ignore for now
	else if (!strncmp(cmd, "shape", len))
		return true;
	else if (!strncmp(cmd, "center", len))
		return true;
	else if (!strncmp(cmd, "tile", len))
		return true;
	else if (!strncmp(cmd, "tileImage", len))
		return true;
	else if (!strncmp(cmd, "sector", len))
		return true;
	else if (!strncmp(cmd, "defaultAltitudeType", len))
		return true;
	else if (!strncmp(cmd, "flyto", len))
		return true;
	else if (!strncmp(cmd, "follow", len))
		return true;
	else if (!strncmp(cmd, "listen", len))
		return true;
    else
    {
        fprintf(stderr, "SdtApp::OnCommand() invalid command: %s\n", cmd);
        return false;
    }
    return true;
}  // end SdtApp::OnCommand() 

bool SdtApp::SetInput(const char* inputFile)
{
	char fileName[FILE_NAME_MAX];

    // Try to find the file as named, or in the specified path
	// note that the inputFile string may be modified below

	if (!inputFile || (strlen(inputFile) > FILE_NAME_MAX - 1))
	{
		PLOG(PL_ERROR, "SdtApp::SetInput - inputFile is NULL or too large\n");
		return false;
	}

    strcpy(fileName, inputFile);  // allows full path to be prepended
	if (!FindFile(fileName))  // returns with full path
	{
		PLOG(PL_ERROR, "SdtApp::SetInput - FindFile failed: %s\n", fileName);
		return false;
	}

	// TBD - verify that fileName is not too long
	// save script file path (assumed 1st file), if present, for next time
	char *lastSlash = strrchr(const_cast<char*>(inputFile), SLASH);
	if (lastSlash != NULL)
	{
		*lastSlash = '\0';  // snip the input string at the slash
		strcpy(script_path, inputFile);  // already limit-checked
	}

    ProtoDispatcher::Descriptor scriptFile = OpenScript(fileName);
	if (ProtoDispatcher::INVALID_DESCRIPTOR == scriptFile)
    {
        PLOG(PL_ERROR, "SdtApp::SetInput - invalid file %s\n", fileName);
        return false;
    }

	// If the cmd is coming from a pipe or from the command
	// line prompt, add the file to the end of the stack for
	// reading after the current file is processed.  Otherwise
	// interrupt processing and read it immediately.
	if (pipe_pending_cmd[0] != '\0' && !strcmp(pipe_pending_cmd, "input"))
	{
		if (!input_stack.AddLast(scriptFile))
		{
			PLOG(PL_ERROR, "SdtApp::SetInput() error pushing current input\n");
			CloseScript(scriptFile);
			return false;
		}

		// If this is our first input file, set current_input
		// otherwise we'll get to it as we cycle through the
		// file stack.
		if (!current_input ||
			(current_input == ProtoDispatcher::INVALID_DESCRIPTOR))
			current_input = scriptFile;
	}
	else  // pipe_pending_cmd
	{
		if (!input_stack.Push(scriptFile))
		{
			PLOG(PL_ERROR, "SdtApp::SetInput() error pushing current input\n");
			CloseScript(scriptFile);
			return false;
		}

		dispatcher.RemoveGenericInput(current_input);
            
		// Force the file to be processed immediately.
		current_input = scriptFile;
	}  // end if, pipe_pending_cmd
        
	if((ProtoDispatcher::INVALID_DESCRIPTOR != current_input) && 
	   (!dispatcher.InstallGenericInput(current_input,
	                                    &SdtApp::GetInputCallBack, this)))
	{
		PLOG(PL_ERROR,
			 "SdtApp::SetInput: failed to install file to dispatcher.\n");
		//return false;  // TBD?
	}

	//PLOG(PL_ERROR, "SdtApp::SetInput() success\n");
	return true;
}  // end SdtApp::SetInput()

bool SdtApp::GetInput()
{
#ifdef WIN32
    DWORD readCount;
    if (console_input && (GetStdHandle(STD_INPUT_HANDLE) == current_input))
    {
        INPUT_RECORD inputRecord;
        if (0 != ReadConsoleInput(current_input, &inputRecord, 1, &readCount))

        {
            if ((inputRecord.EventType == KEY_EVENT) && inputRecord.Event.KeyEvent.bKeyDown)
            {
                char c = inputRecord.Event.KeyEvent.uChar.AsciiChar;
                input_buffer[input_index] = inputRecord.Event.KeyEvent.uChar.AsciiChar;
                
                if (('\r'== c) || ('\n' == c))
                    fprintf(stdout, "\n");
                else
                    fprintf(stdout, "%c", c);
            }
            else
            {
                return true;  // non-applicable event
            }
        }
        else
        {
            fprintf(stderr, "SdtApp::GetInput() ReadConsoleInput() error\n");
            return true;
        }
    }
    else
    {
        if (1 == ReadFile(current_input, input_buffer+input_index, 1, &readCount, NULL))
        {
            if (0 == readCount) return false; // EOF
        }
        else
        {
            fprintf(stderr, "SdtApp::GetInput() ReadFile() error\n");
            return true;
        }
    }
#else
    // (TBD) Get rid of use of fopen(), etc ... doesn't play well with select()
    int result = read(current_input, input_buffer+input_index, sizeof(char)*1);
    if (1 != result)
    {        
        // Assume end of file
        input_index = 0;
        return false;
    }
#endif  // if/else WIN32     

	// if command or val string is not yet done, mark the new char
	// i.e., buffer not full, eol not reached, char not protected space
	if ( (input_index < 511) &&
		 ('\n' != input_buffer[input_index]) && 
	     ('\r' != input_buffer[input_index]) &&
	     (quoting || comment_in_effect ||
	      !isspace(input_buffer[input_index])) )
	{
		if (!quoting && ('#' == input_buffer[input_index]))
			comment_in_effect = true;

		//if (quoting && link_in_effect &&
		//    (':' == input_buffer[input_index]))
		//	input_buffer[input_index] = ';';  // substitute

		if (!comment_in_effect)
		{
			if ('"' == input_buffer[input_index])
				quoting = !quoting;  // mark beginning and end
			else
				input_index++;  // append the character to string
		}

		return true;
	}
    
	// a full string is ready for processing (either cmd or val)
	input_buffer[input_index] = '\0';

    input_index = 0;  // reset for next string
	comment_in_effect = false;
	quoting = false;

    if ('\0' == input_buffer[0]) return true;  // extra white line

    if (seeking_cmd)  // just have first (cmd) part, no val yet
    {
        switch (GetCmdType(input_buffer))
        {
            case CMD_ARG:
                strcpy(current_cmd, input_buffer);
                seeking_cmd = false;  // need to grab val part before processing
                //if (!strcmp(current_cmd, "link"))
                 //   link_in_effect = true;
                break;
            case CMD_NOARG:
                OnCommand(input_buffer, NULL);  // nothing else needed
                break;
            default:
              if (!strncmp(input_buffer,"region",strlen(input_buffer)))
              {
                  current_node = NULL;  // so we don't change last nodes attrs
              }
              else

                fprintf(stderr, "SdtApp::GetInput() invalid command: \"%s\"\n", 
                                input_buffer);
            break;
        }   
    }
    else  // have both cmd and val parts of command
    {
        bool validCmd = OnCommand(current_cmd, input_buffer);
        if (!validCmd)
            fprintf(stderr, "SdtApp::GetInput - command pair [%s] [%s] not valid\n",
                    current_cmd, input_buffer);

        seeking_cmd = true;  // reset for next cmd
        //link_in_effect = false;
    }

	input_buffer[0] = '\0';  // reset to prevent stale values

    return true;
}  // end SdtApp::GetInput()


void SdtApp::OnAnimationTimeout(wxTimerEvent& /*event*/)
{
    Update(true);
}  // end SdtApp::OnAnimationTimeout()


// This is called when the user <doubleclicks> the map
void SdtApp::OnMapDoubleClick(long x, long y)
{
    SdtNode* node = node_list.FindNodeByPosition(x, y);
    if(node)
    {
        fprintf(stdout, "node %s doubleclick\n", node->GetName());
        fflush(stdout);
    }
}  // end SdtApp::OnMapDoubleClick()

// This is called when the user <shiftclicks> the map
void SdtApp::OnMapShiftClick(long x, long y, int max_x, int max_y)
{
    double real_x, real_y;
	if (bg_left<=bg_right)
		real_x = bg_left + (bg_right-bg_left)*x/(double)max_x;
	else
		real_x = bg_left - (bg_left-bg_right)*x/(double)max_x;
	if (bg_top>=bg_bottom)
		real_y = bg_top - (bg_top-bg_bottom)*y/(double)max_y;
	else
		real_y = bg_top + (bg_bottom-bg_top)*y/(double)max_y;
	
	SdtNode* node = node_list.FindNodeByPosition(x, y);
	fprintf(stdout, "shiftclick position %.6f,%.6f", real_x, real_y);
    if(node) fprintf(stdout, ", node %s, nodeposition %.6f,%.6f", node->GetName(), node->GetPosX(), node->GetPosY());
	fprintf(stdout, "\n");
    fflush(stdout);
}  // end SdtApp::OnMapShiftClick()

// This is called when the user destroys a frame by clicking on the X
// button in the upper right corner of the frame
void SdtApp::OnFrameDestruction(SdtFrame* theFrame)
{
    if (theFrame == map_frame)
    {
		//fprintf(stderr, "SdtApp::OnFrameDestruction - deleting popup_list, map_canvas, and map_frame\n");
		// see SdtApp::OnQuit
		popup_list.Destroy();  // prevent popups from sticking around
		delete map_canvas;
        map_canvas = NULL;
		if (map_frame)
			map_frame->Destroy();
        map_frame = NULL;
        running = false;
    }
    else  // must be a popup
    {
        SdtPopup* popup = (SdtPopup*)popup_list.Remove(theFrame);
        if (popup)
        {
            fprintf(stdout, "popdown \"%s\"\n", (const char*)theFrame->GetTitle().mb_str());
            fflush(stdout);
            popup->Destroy();
        }

    }
}  // end SdtApp::OnFrameDestruction()
bool SdtApp::SetPath(const char* path)
{
    if (!path || (strlen(path) > PATH_NAME_MAX - 1))
		return false;

	strcpy(default_path, path);  // already limit-checked
    return true;
} // end SdtApp::SetPath()

bool SdtApp::SetPipe(const char* pipe)
{
	if (!pipe || (strlen(pipe) > PIPE_NAME_MAX - 1))
		return false;

	strcpy(pipe_name, pipe);  // already limit-checked

    if (control_pipe.IsOpen()) 
    {
        control_pipe.Close();
    }
    if (!control_pipe.Listen(pipe_name))
    {
		PLOG(PL_ERROR,"SdtApp::SetPipe() error opening control pipe %s\n",pipe_name);
        return false;
    }  
	return true;

} // end SdtApp::SetPipe()
bool SdtApp::FindFile(char* theFile)
{
	char pathPtr[PATH_NAME_MAX];  // same size as default_path
	char fileName[FILE_NAME_MAX];  // will contain fully qualified file name

	if (!theFile)
		return false;

	size_t fileNameSize = strlen(theFile);
	if (fileNameSize > FILE_NAME_MAX - 1)
		return false;

	PLOG(PL_ERROR,"SdtApp::FindFile() Looking for file>%s\n",theFile);

	FILE* fp = fopen(theFile, "r");
	if (fp)
	{   
		// specified file exists, so use it.
		PLOG(PL_ERROR, "SdtApp::FindFile() - found it (1, as is)\n");
		fclose(fp);
		return true;
	}

	// check for file in paths listed in PATH variable
	strcpy(pathPtr, default_path);  // local copy (already limit-checked)

	// get first path from path list
	char *pathToken = strtok(pathPtr, PATHSEPS);
	while (pathToken != NULL)
	{
		if ((strlen(pathToken) + strlen(FILESEP) + fileNameSize) > FILE_NAME_MAX - 1)
			return false;

		strcpy(fileName, pathToken); // prepend path (TBD limit-check)
		strcat(fileName, FILESEP);  // insert slash between path and file name
		strcat(fileName, theFile);  // append actual file name

		//PLOG(PL_ERROR,"SdtApp::FindFile() - attempting to fopen file>%s\n", fileName);
		fp = fopen(fileName, "r");
		if (fp)  // does this file exist?
		{
			PLOG(PL_ERROR, "SdtApp::FindFile() - found it (2, from path)\n");
			fclose(fp);  // yes
			strcpy(theFile, fileName); // transcribe full path back to original ptr
			return true;
		}

		pathToken = strtok(NULL, PATHSEPS);  // next path (NULL when done)
	}  // end while (pathToken)

	// not in one of paths - look in same directory as current input file
	if (script_path[0] != '\0')
	{
		if ((strlen(script_path) + strlen(FILESEP) + fileNameSize) > FILE_NAME_MAX - 1)
			return false;

		//PLOG(PL_ERROR, "SdtApp::FindFile - using script_path = [%s]\n", script_path);
		strcpy(fileName, script_path);   // start with script path
		strcat(fileName, FILESEP);  // insert slash
		strcat(fileName, theFile);  // append actual file name

		fp = fopen(fileName, "r");
		if (fp)  // does file exist?
		{
			PLOG(PL_ERROR, "SdtApp::FindFile() - found it (3, in script_path)\n");
			fclose(fp);  // yes
			strcpy(theFile, fileName); // transcribe full path
			return true;
		}  // end if (fp)
	} // end if (script_path)

	PLOG(PL_ERROR,"SdtApp::FindFile() File>%s not found\n",theFile);
    return false;  // fall through default

} // SdtApp::FindFile()

bool SdtApp::SetBackgroundImage(const char* fileName)
{
    if (!fileName || (strlen(fileName) > (FILE_NAME_MAX - 1)))
		return false;

    // Try to find the imageFile as named or in the specified path.
    char imageFile[FILE_NAME_MAX];
    strcpy(imageFile, fileName);  // copy for prepending path
    if (!FindFile(imageFile)) return false;

    if (!bg_image.LoadFile(wxString::FromAscii(imageFile)))
    {
        wxLogError(_T("SdtApp::SetBackgroundImage() Couldn't load image from file '%s'."), imageFile);
        return false;
    }    

    // Create new bitmap
	wxBitmap* b = new wxBitmap(bg_image);
    if (!b)
    {
        perror("SdtApp::SetBackgroundImage() error allocating bitmap");
        return false;
    }

	if (!bg_bitmap && (!map_canvas || !map_canvas->SetSize(b->GetWidth(), b->GetHeight())))
	{
		fprintf(stderr, "SdtApp::SetBackgroundImage() error resizing window\n");
		return false;   
	}

	if (bg_bitmap)
		delete bg_bitmap;
	else
		SetBounds(0,0,b->GetWidth()-1, b->GetHeight()-1);

	bg_bitmap = b;
	bg_imagewidth  = bg_bitmap ? bg_bitmap->GetWidth() : 0;
	bg_imageheight = bg_bitmap ? bg_bitmap->GetHeight() : 0;

	Update(false);

	if (map_canvas)
		map_canvas->Refresh();

	return true;
}  // end SdtApp::SetBackgroundImage()

void SdtApp::SetBounds(double left, double top, double right, double bottom)
{
    bg_left = left;
    bg_top = top;
    bg_right = right;
    bg_bottom = bottom;

}  // end SdtApp::SetBounds()

bool SdtApp::InBounds(double x, double y)
{
    double min = (bg_left < bg_right) ? bg_left : bg_right;
    double max = (bg_left > bg_right) ? bg_left : bg_right;
    if ((x < min) || (x > max)) return false;
    min = (bg_top < bg_bottom) ? bg_top : bg_bottom;
    max = (bg_top > bg_bottom) ? bg_top : bg_bottom;
    if ((y < min) || (y > max)) return false;
    return true;
}  // end SdtApp::InBounds()

long SdtApp::RemapX(double x)
{
    double iWidth = (double)(map_canvas ? map_canvas->GetMapWidth() : 0);
    double mWidth = ((bg_right > bg_left) ? (bg_right - bg_left) :
                                            (bg_left - bg_right));
    double scale = iWidth / mWidth;
    double xMin = (bg_left < bg_right) ? bg_left : bg_right;
	//fprintf(stderr, "RemapX: x = %f, xMin = %f, iWidth = %f, mWidth = %f, scale = %f\n", x, xMin, iWidth, mWidth, scale);
    x -= xMin;
    x *= scale;
    x = (bg_left < bg_right) ? x : map_canvas->GetMapWidth() - x;
    return (long)(x+0.5);
};  // end SdtApp::RemapX()

long SdtApp::RemapY(double y)
{
    double iHeight = (double)(map_canvas ? map_canvas->GetMapHeight() : 0);
    double mHeight = (double)((bg_bottom > bg_top) ? (bg_bottom - bg_top) :
                                                     (bg_top - bg_bottom));
    double scale = iHeight / mHeight;
    double yMin = (bg_top < bg_bottom) ? bg_top : bg_bottom;
	//if (y < yMin) {
		//fprintf(stderr, "RemapY: y = %f, yMin = %f, iHeight = %f, mHeight = %f, scale = %f\n", y, yMin, iHeight, mHeight, scale);
		//y = 38.819171218;
	//}
    y -= yMin;
    y *= scale;
    y = (bg_top < bg_bottom) ? y : map_canvas->GetMapHeight() - y;
    return (long)(y+0.5);;
};  // end SdtApp::RemapY()

long SdtApp::RemapXDist(double x)
{
    double iWidth = (double)(map_canvas ? map_canvas->GetMapWidth() : 0);
    double mWidth = ((bg_right > bg_left) ? (bg_right - bg_left) :
                                            (bg_left - bg_right));
    double scale = iWidth / mWidth;
    x *= scale;
    x = (bg_left < bg_right) ? x : -x;
    return (long)(x+0.5);
};  // end SdtApp::RemapX()

long SdtApp::RemapYDist(double y)
{
    double iHeight = (double)(map_canvas ? map_canvas->GetMapHeight() : 0);
    double mHeight = (double)((bg_bottom > bg_top) ? (bg_bottom - bg_top) :
                                                     (bg_top - bg_bottom));
    double scale = iHeight / mHeight;
    y *= scale;
    y = (bg_top < bg_bottom) ? y : -y;
    return (long)(y+0.5);;
};  // end SdtApp::RemapY()

bool SdtApp::SetBackgroundSize(long width, long height)
{
    if (bg_bitmap)
    {
        wxBitmap* b = new wxBitmap(bg_image.Scale(width, height));
        if (!b)
        {
            perror("SdtApp::SetBackgroundSize() error allocating bitmap");
            return false; 
        }    
        delete bg_bitmap;
        bg_bitmap = b;
    }

	if (!map_canvas)
	{
		fprintf(stderr, "SdtApp::SetBackgroundSize - map_canvas is NULL\n");
		return false;
	}

    if (!map_canvas->SetSize(width, height))
    {
        fprintf(stderr, "SdtApp::SetBackgroundSize() error resizing window\n");
        return false; 
    }

    int oldWidth, oldHeight;
    map_canvas->GetVirtualSize(&oldWidth, &oldHeight);
    if ((width < oldWidth) || (height < oldHeight)) 
    {
        // (TBD) move this back into the canvas set size
        map_canvas->ClearBackground(); 
    }

    Update(false);

	map_canvas->Refresh();
    return true;
}  // end SdtApp::SetBackgroundSize()

bool SdtApp::ScaleBackground(double factor)
{
	if (!map_canvas)
		return false;

    long newWidth = (long)((double)map_canvas->GetMapWidth() * factor + 0.5);
    long newHeight = (long)((double)map_canvas->GetMapHeight() * factor + 0.5);
    return SetBackgroundSize(newWidth, newHeight);
}  // end SdtApp::ScaleBackground()

bool SdtApp::SetSprite(const char* name)
{
	current_sprite = sprite_list.FindSpriteByName(name);
	if (current_sprite)
		return true;  // already exists so exit

	// need to create sprite
	current_sprite = new SdtSprite();
	if (!current_sprite || !current_sprite->SetName(name))
	{
		delete current_sprite;  // will work on NULL ptr
		current_sprite = NULL;
		return false;
	}

    sprite_list.Append(current_sprite);  // add new sprite to list
    return true;
}  // end SdtApp::SetSprite()

bool SdtApp::SetStatus(const char* name)
{
    // Delete the old status if it exists
    if (node_list.FindNodeByName(status_node))
      DeleteNode(status_node);

    if (!strcmp(name, "none") || !strcmp(name, "NONE"))
      return true;
                                       
    strcpy(status_node, "Status: ");
	strncat(status_node, name, STATUS_MAX - 1 - strlen("Status: "));
    
    // Since we're juryrigging status by using a node, squirrel
    // away a copy of the actual current node...
    SdtNode* actual_current_node = current_node;
    SetNode(status_node);
    int xscale, yscale;
	if (!map_canvas)
		return false;

    map_canvas->GetScrollPixelsPerUnit(&xscale, &yscale);
    double left_offset = (bg_left - bg_right)/xscale;
    double top_offset = (bg_top - bg_bottom)/yscale;
    
    SetNodeNumPosition(bg_left - left_offset, bg_top - top_offset);
    current_node = actual_current_node;
    return true;
}

bool SdtApp::SetNode(const char* name)
{
	// assumes that "name" is NULL-checked by FNBN and SN
	current_node = node_list.FindNodeByName(name);
	if (current_node)
		return true;  // node already exists so exit

	current_node = new SdtNode();
	if (!current_node || !current_node->SetName(name))
	{
		delete current_node;  // will work on NULL ptr
		current_node = NULL;
		return false;
	}

	// Assign first sprite in list as default node type
	SetNodeType("default");
	//fprintf(stderr, "SdtApp::SetNode - adding node %s\n", name);
	node_list.Append(current_node);  // add new node to list
	return true;
}  // end SdtApp::SetNode()

bool SdtApp::DeleteNode(const char* name)
{
    SdtNode* node = node_list.FindNodeByName(name);
    if (node)
    {
        // Delete any links involving this node
        link_list.RemoveLinks(node);
        node_list.Remove(node);
        delete node;
        return true;
    }
    else
    {
        return false;   
    }    
}  // end SdtApp::DeleteNode()

bool SdtApp::SetNodeType(const char* spriteName)
{
    if (current_node)
    {
        if (spriteName && strcmp(spriteName, "none"))
        {
            SdtSprite* sprite = sprite_list.FindSpriteByName(spriteName);
            if (sprite)
            {                
                current_node->SetSprite(sprite);
                return true;
            }
            else
            {
                fprintf(stderr, "SdtApp::SetNodeType() error: invalid sprite name\n");
                return false;
            }
        }
        else
        {
            current_node->SetSprite(NULL);
            return true;
        }
    }
    else
    {
        fprintf(stderr, "SdtApp::SetNodeType() error: no node specified\n");
        return false;   
    }
}  // end SdtApp::SetNodeType()

bool SdtApp::SetNodeNumPosition(double x, double y)
{
    if (!current_node)
    {
        fprintf(stderr, "SdtApp::SetNodeNumPosition() error: no node specified\n");
        return false;   
    }   

	current_node->SetPosition(x, y);
	return true;
}

bool SdtApp::SetNodePosition(const char* xpos, const char* ypos)
{
	double x, y;

    if (!current_node)
    {
        fprintf(stderr, "SdtApp::SetNodePosition() error: no node specified\n");
        return false;   
    }   

	if ((xpos == NULL) || !strcmp(xpos, "X"))
		x = current_node->GetPosX();  // get previous value
	else
		x = atof(xpos);

	if ((ypos == NULL) || !strcmp(ypos, "X"))
		y = current_node->GetPosY();  // get previous value
	else
		y = atof(ypos);

	current_node->SetPosition(x, y);
	return true;
}  // end SdtApp:SetNodePosition()

bool SdtApp::AddLink(const char* src, const char* dst,
                     const char* linkID, const char* directed)
{
	if (!src || !dst)
	{
		PLOG(PL_ERROR, "SdtApp::AddLink - link has NULL src (%s) / dst (%s)\n",
		               src, dst);
		return false;
	}

    SdtNode* srcNode = node_list.FindNodeByName(src);
    if (!srcNode)
    {
        PLOG(PL_ERROR, "SdtApp::AddLink() invalid src: \"%s\"\n", src);
        return false;
    } 
    SdtNode* dstNode = node_list.FindNodeByName(dst);
    if (!dstNode)
    {
        PLOG(PL_ERROR, "SdtApp::AddLink() invalid dst: \"%s\"\n", dst);
        return false;
    }   

	current_link = NULL;  // reset in case this is an "all" option

	current_src = NULL;  // reset for every link command
	current_dst = NULL;
	current_directedness = NO_ALLS;
	current_link_ID[0] = '\0';

	// is there an "all,*" option in effect? (* = "all" or "dir")
	if (linkID && !strcmp(linkID, "all"))
	{
		bool validAllStar = true;  // default

		current_src = srcNode;  // an "all,*" option is in effect
		current_dst = dstNode;

		//fprintf(stderr, "SdtApp::AddLink, \"all\" option, directedness = %s\n", directed);

		if (!directed)
			current_directedness = BI_ONLY;
		else if (!strcmp(directed, "dir"))
			current_directedness = UNI_ONLY;
		else if (!strcmp(directed, "all"))
			current_directedness = BI_AND_BOTH_UNI;
		else
			validAllStar = false;  // not a valid "all,*" option

		return validAllStar;
	}

	// is "<linkID>,all" in effect? (already know that linkID != "all")
	if (directed && !strcmp(directed, "all"))
	{  // set up for SetLine loops
		current_src = srcNode;
		current_dst = dstNode;
		current_directedness = SAME_LINKID;
		if (linkID)
			strcpy(current_link_ID, linkID);

		return true;  // SetLine will take care of setting links later
	}

	// if bi-directional, remove any existing uni-directional links 1st
	if (!directed)
	{
		link_list.RemoveLink(srcNode, dstNode, linkID, "dir");
		link_list.RemoveLink(dstNode, srcNode, linkID, "dir");
	}

	// if uni-directional, remove any existing bi-directional &
	// add an additional uni-directional in the opposite direction
	if (directed && !strcmp(directed, "dir"))
	{
		// is there a bi-directional link already?
		SdtLink* chkLink = link_list.FindLink(srcNode, dstNode, linkID, NULL);
		if (chkLink != NULL)
		{
			// remove any existing bi-directional link
			link_list.RemoveLink(srcNode, dstNode, linkID, NULL);
			// add a directed link in the opposite direction
			link_list.AddLink(dstNode, srcNode, linkID, "dir");
		}
	}

	// now add (reset) the link
	current_link = link_list.AddLink(srcNode, dstNode, linkID,
	                                 directed);
	if (!current_link)
	{
		PLOG(PL_ERROR, "SdtApp::AddLink - adding (%s, %s) link to list failed\n",
		        srcNode->GetName(), dstNode->GetName());
		return false;
	}

	return true;  // color and thickness, if not defaults, added later
}  // end SdtApp::AddLink()

bool SdtApp::RemoveLink(const char* src, const char* dst,
                        const char* linkIDorAll, const char* dirOrAll)
{
	if (!src || !dst)
		return false;

    SdtNode* srcNode = node_list.FindNodeByName(src);
    if (!srcNode)
    {
        fprintf(stderr, "SdtApp::RemoveLink() invalid src\n");
        return false;
    } 

    SdtNode* dstNode = node_list.FindNodeByName(dst);
    if (!dstNode)
    {
        fprintf(stderr, "SdtApp::RemoveLink() invalid dst\n");
        return false;
    }   

	//fprintf(stderr, "RemoveLink - deleting link (%s, %s, %s, %s)\n",
	//        src, dst, linkIDorAll, dirOrAll);

	// NULL or valid linkID ("all" is reserved) without "all" dir
	if ((!linkIDorAll || strcmp(linkIDorAll, "all")) &&
	    (!dirOrAll || strcmp(dirOrAll, "all")) )
	{
		SdtLink* chkLink = link_list.FindLink(srcNode, dstNode, linkIDorAll, dirOrAll);
		if (chkLink == current_link)
			current_link = NULL;
		return (link_list.RemoveLink(srcNode, dstNode, linkIDorAll,
	                                 dirOrAll));
	}

	DirType linkDirectedness = NO_ALLS;  // default to invalid option
	// is it "<linkID>,all"?
	if ((!linkIDorAll || strcmp(linkIDorAll, "all")) &&
	    (dirOrAll && !strcmp(dirOrAll, "all")))
	{
		//fprintf(stderr, "RemoveLink - found a  <linkID>,all\n");
		linkDirectedness = SAME_LINKID;
	}
	else if (!dirOrAll)  // from now on it is an "all,*" option
		linkDirectedness = BI_ONLY;
	else if (!strcmp(dirOrAll, "dir"))
		linkDirectedness = UNI_ONLY;
	else if (!strcmp(dirOrAll, "all"))
		linkDirectedness = BI_AND_BOTH_UNI;
	//else take default which will make loop fail below

	bool removeOK = false; // default to nothing removed

	SdtLink* nextLink = link_list.Head();
	while (nextLink)
	{
		SdtLink* currentLink = nextLink;
		nextLink = currentLink->Next();  // in case it is deleted below
		if (MatchingLink(srcNode, dstNode, linkDirectedness, currentLink))
		{
			if (currentLink == current_link)
				current_link = NULL;
			link_list.Remove(currentLink);
			delete currentLink;
			removeOK = true;  // at least one removed
		}
	}

	return removeOK;
}  // end SdtApp::RemoveLink()

void SdtApp::Update(bool advance)
{
    wxBitmap* map = map_canvas ? map_canvas->GetBitmap() : NULL;
    if (map)
    {
        // TBD - we should maintain the wxMemoryDC "mdc" as an SdtApp member variable
        // and update it or redraw it only when things change (up to the animation rate)
        // (i.e. add a bool that is set to indicate that something has changed since the last
        //  time SdtApp::Update() was called and only invoke this code when such a change occurs
        // ... and we could also do the Blit() below on only an "as needed" basis, too 
        
        int width, height;
        map_canvas->GetClientSize(&width, &height);
        int xscale, yscale;
        map_canvas->GetScrollPixelsPerUnit(&xscale, &yscale);
        int x, y;
        map_canvas->GetViewStart(&x, &y);
        x *= xscale;
        y *= yscale;
        
        wxMemoryDC mdc;
        mdc.SelectObject(*map);
        mdc.SetBackgroundMode(wxSOLID);
        mdc.SetClippingRegion(x, y, width, height);
        //mdc.BeginDrawing();  - Adamson 2.8 check
        
        // Draw background
        if (bg_bitmap) 
            mdc.DrawBitmap(*bg_bitmap, 0,0, false);
        else
            mdc.Clear();

        // Draw links
        wxPen pen;
        SdtLink* nextLink = link_list.Head();
        while (nextLink)
        {
            pen.SetColour(nextLink->GetColor());
            pen.SetWidth(nextLink->GetThickness());
            mdc.SetPen(pen);
            mdc.SetBrush(*wxTRANSPARENT_BRUSH);
            const SdtNode* src = nextLink->Src();
            const SdtNode* dst = nextLink->Dst();

			// default case - draw straight line only for non-multiple link
			if (nextLink->IsHeadMultOrSingle())
			{
				mdc.DrawLine(RemapX(src->GetPosX()), RemapY(src->GetPosY()),
				             RemapX(dst->GetPosX()), RemapY(dst->GetPosY()));
			}

			if (mlinks_on && nextLink->IsHeadMult())  // draw all multiples, if any, for this link
			{
				nextLink->CalcArcParams();  // recalculate each time
				nextLink->RemapNodeSwap();  // correct remapped src/dst

				long xCenter = nextLink->GetCenterX();
				long yCenter = nextLink->GetCenterY();

				long xInc = nextLink->GetIncX(nextLink);
				long yInc = nextLink->GetIncY(nextLink);

				long xSrc = nextLink->GetRemappedSrcX();
				long ySrc = nextLink->GetRemappedSrcY();

				long xDst = nextLink->GetRemappedDstX();
				long yDst = nextLink->GetRemappedDstY();

				bool evenLink = true;  // initialize

				SdtLink* nextMult = nextLink->GetNextMlink();  // first non-head, multiple link, if present
				while (nextMult)
				{
					//wxString color = nextMult->GetColor().GetAsString();
					//fprintf(stderr, "Update - drawing arc for %s, head is (%s, %s, ID: %s), center = (%ld, %ld), color is %s\n",
					//        nextMult->GetLinkID(), src->GetName(), dst->GetName(), nextLink->GetLinkID(), xCenter, yCenter, color.c_str());
					pen.SetColour(nextMult->GetColor());
					pen.SetWidth(nextMult->GetThickness());
					mdc.SetPen(pen);

					if (evenLink)
						mdc.DrawArc(xSrc, ySrc, xDst, yDst,
						            xCenter, yCenter);
					else  // flip circle center to other side of link
						mdc.DrawArc(xDst, yDst, xSrc, ySrc,
					                xSrc + xDst - xCenter,
					                ySrc + yDst - yCenter);

					xCenter -= xInc;  // adjust center for each link
					yCenter -= yInc;

					evenLink = !evenLink;  // flip it for next time
					nextMult = nextMult->GetNextMlink();
				}
			}

			if (nextLink->LinkLabelState() == SdtLink::LABEL_ON)
			{
				wxCoord tw = 0, th = 0;  // needed for wxW 2.9+
				mdc.SetTextBackground(nextLink->GetLinkLabelColor());
				mdc.GetTextExtent(wxString::FromAscii(nextLink->GetLinkLabelText()), &tw, &th);

				double linkDenom = dst->GetPosX() - src->GetPosX();
				int linkSlant = 1;  // default - link slants positive
				if ((abs(linkDenom) < .00001) || (((dst->GetPosY() - src->GetPosY()) / linkDenom) < 0))
					linkSlant = -1;  // link slants negative or is almost vertical

				double xmid = (dst->GetPosX() + src->GetPosX()) / 2;
				double ymid = (dst->GetPosY() + src->GetPosY()) / 2;
				long x = RemapX(xmid);
				long y = RemapY(ymid) + linkSlant * (int)th;

				mdc.DrawText(wxString::FromAscii(nextLink->GetLinkLabelText()), x, y);
			}

            nextLink = nextLink->Next();  
        }

        //  Draw nodes
        SdtNode* nextNode = node_list.Head();
        while (nextNode)
        {   
            wxCoord tw=0, th=0;
            mdc.SetTextBackground(nextNode->GetLabelColor());
            const wxBitmap* nodeMap = nextNode->GetBitmap(advance);
            long x = RemapX(nextNode->GetPosX());
            long y = RemapY(nextNode->GetPosY());
			//fprintf(stderr, "SdtApp::Update - drawing a node at (%ld, %ld)\n", x, y);
            long nodeWidth = nodeMap ? nodeMap->GetWidth() : 0;
            long nodeHeight = nodeMap ? nodeMap->GetHeight() : 0;
            long totalNodeHeight = nodeHeight;
            long totalNodeWidth = nodeWidth;
            if (nodeMap)
            {
                // (TBD) check bounds
                mdc.DrawBitmap(*nodeMap, 
                               x - nodeWidth/2, 
                               y - nodeHeight/2, 
                               true); 
            }
            if (nextNode->Label())
            {
                mdc.GetTextExtent(wxString::FromAscii(nextNode->GetLabelText()), &tw, &th);
                totalNodeHeight += (int)th;
                totalNodeWidth = (int)tw > totalNodeWidth ? tw : totalNodeWidth;
                y = nodeHeight ? y + nodeHeight/2 : y - (int)th/2;
                mdc.DrawText(wxString::FromAscii(nextNode->GetLabelText()), 
                             x - (int)tw/2, y);
                             
                // put x,y back in middle of the node (middle of whole node, not just the sprite)
               y = y - nodeHeight/2 + (int)th/2;
            }
            
            // symbols
            wxPen symPen;
            symPen.SetColour(nextNode->GetSymbolColor());
            int thickness = nextNode->GetSymbolThickness();
            symPen.SetWidth(thickness);
            mdc.SetPen(symPen);
            mdc.SetBrush(*wxTRANSPARENT_BRUSH);
            if (nextNode->GetSymbol() == SdtNode::ELLIPSE)
            {
                // x,y currently == center of ellipse
                // need x,y = upper-left corner of rectangle which the ellipse will go into
                
                long x_radius, y_radius;
                
                double radius = nextNode->GetCircleXRadius();
                double radius2 = nextNode->GetCircleYRadius();
                if (radius && radius2)
                {
                    // this represents a "circle" within the coordinate system you have assigned
                    // we cannot actually draw a circle, as SDT's aspect ratio may change (Ctrl-A)
                    x_radius = RemapXDist(radius);
                    y_radius = RemapYDist(radius2);
                }
                else
                {
                    // set some points on ellipse
                    double x1 = (double)totalNodeWidth/2;
                    double y1 = (double)totalNodeHeight/2;
                    double x2 = (double)totalNodeWidth/2 + 0.5;
                    double y2 = -(double)totalNodeHeight/2 + 0.5;
                    
                    double b = sqrt((x1*x1*y2*y2 - x2*x2*y1*y1) / (x1*x1-x2*x2));
                    double a = b*x1/sqrt(b*b-y1*y1);
                    x_radius = (long)(a + 0.5);
                    y_radius = (long)(b + 0.5);
                }
                // set x,y to upper-left corner of ellipse
                x -= x_radius;
                y -= y_radius;
                
                long ellipsewidth = 2*x_radius;
                long ellipseheight = 2*y_radius;
                
                mdc.DrawEllipse(x, y, ellipsewidth, ellipseheight);
            }
            if (nextNode->GetSymbol() == SdtNode::CIRCLE)
            {
                // need x,y = center of circle; already there.
                double radius = nextNode->GetCircleXRadius();
                double radius2 = nextNode->GetCircleYRadius();
                if (radius && radius2)
                {
                    // this represents a "circle" within the coordinate system you have assigned
                    // we cannot actually draw a circle, as SDT's aspect ratio may change (Ctrl-A)
                    //long x_radius = RemapXDist(radius);
                    //long y_radius = RemapYDist(radius2);
                    
                    // need x,y = upper-left corner
                    //x -= x_radius;
                    //y -= y_radius;
                    x -= radius;
                    y -= radius2;
                    
                    //long ellipsewidth = 2*x_radius;
                    //long ellipseheight = 2*y_radius;
                    long ellipsewidth = 2 * radius;
                    long ellipseheight = 2 * radius2;
                    mdc.DrawEllipse(x, y, ellipsewidth, ellipseheight);
                }
                else
                {
                    // circle radius = pythagorean theorem w/ height/2, width/2
                    double a = (double)totalNodeHeight/2;
                    double b = (double)totalNodeWidth/2;
                    long drawradius = (long) (sqrt(a*a + b*b) + thickness/2 + 0.5);
                    mdc.DrawCircle(x, y, drawradius);
                }
            }
            else if (nextNode->GetSymbol() == SdtNode::RECTANGLE)
            {
                // need x,y = upper-left corner of rectangle
                x -= (long) ((double)totalNodeWidth/2 + (double)thickness/2 + 0.5);
                y -= (long) ((double)totalNodeHeight/2 + (double)thickness/2 + 0.5);
                
                // calculate width and height
                long rectwidth = totalNodeWidth + thickness + 1;
                long rectheight = totalNodeHeight + thickness + 1;
                
                mdc.DrawRectangle(x, y, rectwidth, rectheight);
            }
            else if (nextNode->GetSymbol() == SdtNode::SQUARE)
            {
                // need x,y = upper-left corner of square
                long biggestside = totalNodeWidth>totalNodeHeight ? totalNodeWidth : totalNodeHeight;
                x -= (long) ((double)biggestside/2 + (double)thickness/2 + 0.5);
                y -= (long) ((double)biggestside/2 + (double)thickness/2 + 0.5);
                
                // calculate width and height
                long rectwidth = biggestside + thickness + 1;
                long rectheight = biggestside + thickness + 1;
                
                mdc.DrawRectangle(x, y, rectwidth, rectheight);
            }
            else if (nextNode->GetSymbol() == SdtNode::RND_RECTANGLE)
            {
                // assumption is rounded corner radius = 35% width of smallest side...
                double radiuspercent = 0.35;    // ratio of radius length/length of smallest side
                // NOTE:  this is the smallest side of the invisible rectangle containing the node,
                //        not the rectangle being drawn!
                
                // calculate corner radius
                long smallestside = totalNodeWidth<totalNodeHeight ? totalNodeWidth : totalNodeHeight;
                long radiuslength = (long) ((double)smallestside * radiuspercent);
                // caclulate amount to expand rectangle over node area
                double rectexpansion = (double)radiuslength*(2 - sqrt((double)2));
                
                // need x,y = upper-left corner of rectangle
                x -= (long) ((double)totalNodeWidth/2 + (double)thickness/2 + rectexpansion/2 + 0.5);
                y -= (long) ((double)totalNodeHeight/2 + (double)thickness/2 + rectexpansion/2 + 0.5);
                
                // calculate width and height
                long rectwidth = totalNodeWidth + thickness + (long)(rectexpansion + 0.5);
                long rectheight = totalNodeHeight + thickness + (long)(rectexpansion + 0.5);
                
                mdc.DrawRoundedRectangle(x, y, rectwidth, rectheight, radiuslength);
            }
            else if (nextNode->GetSymbol() == SdtNode::RND_SQUARE)
            {
                // assumption is rounded corner radius = 35% width of side...
                double radiuspercent = 0.35;    // ratio of radius length/length of side
                // NOTE:  this is the smallest side of the invisible rectangle containing the node,
                //        not the rectangle being drawn!
                
                // calculate corner radius
                long biggestside = totalNodeWidth>totalNodeHeight ? totalNodeWidth : totalNodeHeight;
                long radiuslength = (long) ((double)biggestside * radiuspercent);
                // caclulate amount to expand rectangle over node area
                double rectexpansion = (double)radiuslength*(2 - sqrt((double)2));
                
                // need x,y = upper-left corner of rectangle
                x -= (long) ((double)biggestside/2 + (double)thickness/2 + rectexpansion/2 + 0.5);
                y -= (long) ((double)biggestside/2 + (double)thickness/2 + rectexpansion/2 + 0.5);
                
                // calculate width and height
                long rectwidth = biggestside + thickness + (long)(rectexpansion + 0.5);
                long rectheight = biggestside + thickness + (long)(rectexpansion + 0.5);
                
                mdc.DrawRoundedRectangle(x, y, rectwidth, rectheight, radiuslength);
            }
            // symbols complete, restore brush, pen
            mdc.SetBrush(wxNullBrush);
            mdc.SetPen(wxNullPen);
            
            nextNode = nextNode->Next();
        }
        // mdc.EndDrawing(); - Adamson 2.8 check
        mdc.DestroyClippingRegion();

        wxClientDC dc(map_canvas);
        // set clipping on canvas to visible, + 1 pixel around to fix strange artifact problem
        dc.SetClippingRegion(map_canvas->wxWindow::GetRect().Inflate(1));
        //dc.BeginDrawing();- Adamson 2.8 check
        dc.Blit(0, 0, width, height, &mdc, x, y);
        //dc.EndDrawing();- Adamson 2.8 check
        dc.DestroyClippingRegion();

        mdc.SelectObject(wxNullBitmap);
    }  // end if (map)
}  // end SdtApp::Update()

bool SdtApp::OnInit()
{
#ifdef WIN32
//    Moved below statement to OnCommand() function block:
//    current_input = GetStdHandle(STD_INPUT_HANDLE);
#else
    current_input = STDIN_FILENO;
    /*int fd = fileno(stdin);
    if(-1 == fcntl(fd, F_SETFL, fcntl(fd, F_GETFL, 0) | O_NONBLOCK))
        perror("SdtApp::OnInit() fcntl(F_SETFL(O_NONBLOCK)) error");*/
#endif

    wxInitAllImageHandlers();
    
    // Allocate and create map frame
    if (!(map_frame = new SdtFrame(this)))
    {
        perror("SdtApp::OnInit() error allocating map window");
        return false;   
    }

    // Create the SDT main window
    memset(title, CMD_LINE_MAX, 0);
    strcpy(title,"Scripted Display Tool");

    if (map_frame->Create(title))
    {
        // Create menu for our main frame
        wxMenuBar* menuBar = new wxMenuBar();
        if (!menuBar)
        {
            fprintf(stderr, "SdtApp::OnInit() error creating menu bar\n");
            return false;
        }
        wxMenu* menu = new wxMenu();
        if (!menu)
        {
            fprintf(stderr, "SdtApp::OnInit() error creating file menu\n");
            return false;
        }
        menu->Append(SdtApp::ID_FILEOPEN, _T("&Open File...\tCTRL-O"));
        menu->Append(SdtApp::ID_QUIT, _T("E&xit\tCTRL-Q"));
        menuBar->Append(menu, _T("&File"));

        menu = new wxMenu();
        if (!menu)
        {
            fprintf(stderr, "SdtApp::OnInit() error creating options menu\n");
            return false;
        }
        menu->Append(ID_AUTO, _T("&Auto-Size\tCTRL-S"));
    	menu->Append(ID_FILL, _T("&Fill Window\tCTRL-F"));
        menu->Append(ID_SCRCAP, _T("&Capture Screen\tCTRL-P"));
        menu->Append(ID_LABELS, _T("&Toggle Link Labels\tCTRL-L"));
		menu->Append(ID_MLINKS, _T("&Toggle Multiple Links\tCTRL-M"));
        menuBar->Append(menu, _T("&Options"));

        menu = new wxMenu();
        if (!menu)
        {
            fprintf(stderr, "SdtApp::OnInit() error creating help menu\n");
            return false;
        }
        menu->Append(ID_ABOUT, _T("&About sdt ..."));
        menuBar->Append(menu, _T("&Help"));
        
        map_frame->SetMenuBar(menuBar);
        
        map_frame->SetTitle(_T("Scripted Display Tool"));
    }
    else
    {
        perror("SdtApp::OnInit() error creating map frame");
        delete map_frame;
        return false;
    }
    // Allocate and create scrollable map canvas
    if (!(map_canvas = new SdtMapCanvas(this)))
    {
        perror("SdtApp::OnInit() error allocating map canvas");
        delete map_frame;
        return false;
    }
    if (!map_canvas->Create((wxWindow*)map_frame))
    {
        perror("SdtApp::OnInit() error creating map canvas");
        if (map_canvas) delete map_canvas;
        delete map_frame;
        return false;
    }  
    SetBounds(0,0,299,299); // corresponds to default canvas size
          
    map_frame->Show(true);
    
    // Force cmd line input files to be treated as "piped commands"
    // so that input files are read serially
    strcpy(pipe_pending_cmd,"input");  

    // Process command-line options
    int i = 1;
    while (i < argc)
    {
		if (argv[i][0] == '#')
			continue;  // comment, so skip

        wxString myarg(argv[i]);
        switch (GetCmdType((const char *)myarg.mb_str()))
        {
            case CMD_INVALID:
                fprintf(stderr, "SdtApp::OnInit() Invalid command: %s\n", (const char *)myarg.mb_str());
                return false;

            case CMD_NOARG:
                if (!OnCommand((const char *)(const char *)myarg.mb_str(), NULL))
                {
                    fprintf(stderr, "SdtApp::OnInit() OnCommand(%s) error\n", 
                            (char *)(const char *)myarg.mb_str());
                    return false;

                }
                i++;
                break;

            case CMD_ARG:
                wxString myarg2(argv[i+1]);
                if (!OnCommand((const char *)myarg.mb_str(), (const char *)myarg2.mb_str()))

                {
                    fprintf(stderr, "SdtApp::OnInit() OnCommand(%s, %s) error\n", 
                            (const char *)myarg.mb_str(), (const char *)myarg2.mb_str());
                    return false;

                }
                i += 2;
                break;
        }
    }   

    // Reset pipe_pending_cmd so future file/pipe interleaves work
    // correctly.
    pipe_pending_cmd[0] = '\0';

#ifdef WIN32
    // Create a console if needed
    if (console_input)
    {
        AllocConsole();
        int hCrt = _open_osfhandle((long) GetStdHandle(STD_OUTPUT_HANDLE), _O_TEXT);
        FILE* hf = _fdopen(hCrt, "w" );
        *stdout = *hf;
        setvbuf(stdout, NULL, _IONBF, 0 );

        hCrt = _open_osfhandle((long) GetStdHandle(STD_INPUT_HANDLE), _O_TEXT);
        hf = _fdopen(hCrt, "r" );
        *stdin = *hf;
        setvbuf(stdin, NULL, _IONBF, 0);

        hCrt = _open_osfhandle((long) GetStdHandle(STD_ERROR_HANDLE), _O_TEXT);
        hf = _fdopen(hCrt, "w" );
        *stderr = *hf;
        setvbuf(stderr, NULL, _IONBF, 0);

        HANDLE consoleHandle = GetStdHandle(STD_INPUT_HANDLE);
        SetConsoleMode((HANDLE)consoleHandle, ENABLE_PROCESSED_INPUT);
    }
    else if (current_input == GetStdHandle(STD_INPUT_HANDLE))
    {
        // Remove the unneeded Windows STD_INPUT_HANDLE
        current_input = ProtoDispatcher::INVALID_DESCRIPTOR;
    }
#endif // WIN32    
    
#ifdef __WXMAC__
    // This code lets the "sdt" executable run as a MacOS "APPL" without
    // requiring the "sdt.app" application bundle to be used
    //ProcessSerialNumber psn;
    //GetCurrentProcess(&psn);
    //TransformProcessType(&psn, kProcessTransformToForegroundApplication);
    
#endif // __WXMAC__
    
    if (control_pipe.IsOpen()) 
    {
        control_pipe.Close();
    }
    
    if (!control_pipe.Listen(pipe_name))
    {
        PLOG(PL_ERROR, "SdtApp::OnInit() error opening control pipe %s\n",pipe_name);
        SetPopup("Error Opening Control Pipe");
        SetPopupContent("Error: Another application may \nbe using the named pipe \"sdt\".\nPlease check and try again.\n");
        if (current_popup)
		{
			// add OK button
			//wxBoxSizer* sizer = new wxBoxSizer(wxHORIZONTAL);
			wxBoxSizer* sizer = new wxBoxSizer(wxVERTICAL);
			if (!sizer)
				return false;

			// (TBD) add spacer here to force buttons to right side
			sizer->Add(0, 0, 1, wxEXPAND);
			wxButton* okButton = new wxButton(current_popup, wxID_OK, _T("OK"));
			if (!okButton)
				return false;

			sizer->Add(okButton, 0, wxALIGN_RIGHT);
			current_popup->SetSizerAndFit(sizer);

			current_popup->ResizeErrorPopup();  // ensure readability
		}
		// allow fall through
		//return false;
    }
     //Update(true); 
    ActivateTimer(update_timer);
    //animation_timer.Start(100);
    if((ProtoDispatcher::INVALID_DESCRIPTOR != current_input) && 
       (!dispatcher.InstallGenericInput(current_input, &SdtApp::GetInputCallBack,this)))
    {
        printf("Error: failed to install file to dispatcher.\n");
    }
    return true;
}  // end SdtApp::OnInit()

bool SdtApp::OnPipeCommand(char* input_buffer)
{   
    char tempPtr[SOCKET_MAX];
	//fprintf(stderr, "SdtApp::OnPipeCommand - input_buffer = [%s]\n", input_buffer);
    if (1 != sscanf(input_buffer, "%s", tempPtr)) return false;
	//fprintf(stderr, "SdtApp::OnPipeCommand - tempPtr = [%s]\n", tempPtr);
    while (tempPtr[0] != '\0')
    {
        // Get the quoted string, if any
        if ('"' == tempPtr[0])
        {

            // find it in the input_buffer
            while (('\"' != *input_buffer) || ('\0' == *input_buffer)) input_buffer++;
            char* tmpBuffer = ++input_buffer;  // start of quoted string
            int cnt = 0;
            while (('\"' != *input_buffer) || ('\0' == *input_buffer)) input_buffer++, cnt++;
			if (cnt > PIPE_MAX - 1)
				return false;

            // Get the quoted string
            strncpy(tempPtr, tmpBuffer, cnt);
            tempPtr[cnt] = '\0';
        }
        input_buffer += strlen(tempPtr);  // point past latest input
        if ('\0' == pipe_pending_cmd[0])
          strcpy(pipe_pending_cmd,tempPtr);

        if (pipe_seeking_cmd)
        {            
			if (pipe_pending_cmd[0] == '#')
				continue;  // skip comment

            switch (GetCmdType(pipe_pending_cmd))
            {
                
            case CMD_ARG:
              strcpy(pipe_current_cmd,pipe_pending_cmd);
              pipe_seeking_cmd = false;
              break;
            
            case CMD_NOARG:
              OnCommand(pipe_pending_cmd,NULL);
              pipe_pending_cmd[0] = '\0';
              pipe_seeking_cmd = true;  // not needed
              break;

            default:
              pipe_seeking_cmd = true;  // not needed
              pipe_pending_cmd[0] ='\0';
              return false;
            }  // end switch

        }
        else
        {
			//fprintf(stderr, "SdtApp::OnPipeCommand - calling OnCommand with [%s] [%s]\n", pipe_current_cmd, tempPtr);
            OnCommand(pipe_current_cmd,tempPtr);
            pipe_seeking_cmd = true;
            pipe_pending_cmd[0] = '\0';
        }
        while ((' ' == *input_buffer) || ('\t' == *input_buffer)) input_buffer++;
        if ((*input_buffer == '\0') || (1 != sscanf(input_buffer,"%s",tempPtr)))
          break;
		//fprintf(stderr, "SdtApp::OnPipeCommand - latest input_buffer is [%s], new tempPtr is [%s]\n", input_buffer, tempPtr);
    }  // end while (tempPtr)
    return true;
}  // end SdtApp::OnPipeCommand

void SdtApp::OnControlMsg(ProtoSocket&        /* thePipe*/, 
                          ProtoSocket::Event theEvent)
{

    //  fprintf(stderr,"LJT SdtApp::OnControlMsg() \n");
    // To deconflict with file reads, rework w/msg passing object.
    SdtNode* tmp_node = current_node;
    SdtSprite* tmp_sprite = current_sprite;
    current_node = pipe_current_node;
    current_sprite = pipe_current_sprite;
    
    if (ProtoSocket::RECV == theEvent)
    {
        char buffer[SOCKET_MAX];
		buffer[0] = '\0';
        unsigned int len = SOCKET_MAX - 1;  // max allowed
        if (control_pipe.Recv(buffer, len))
        {
			buffer[len] = '\0';  // len should be < SOCKET_MAX
			char *msg = buffer;
			bool pipeQuoting = false;  // initialize

            for (unsigned int i = 0; i < len; i++)
            {
               if ('\n' == buffer[i] || '\r' == buffer[i])
                {
				  PLOG(PL_ERROR,"OnControlMsg: end of line breaking %s\n",buffer);
				  buffer[i] = ' ';  // substitute a space for below
				  pipeQuoting = false; // in case still on
                }
			    // Handle any null terminated strings that windows sometimes adds
			    if ('\0' == buffer[i])
				{
					pipeQuoting = false;
					msg = &(buffer[i+1]); // prepare next message
				}
				if (pipeQuoting)  // no space checking
				{
					if (buffer[i] == '\"')
						pipeQuoting = false;  // quoting ends here
				}
				else  // check for spaces and new quoting
				{
					if (buffer[i] == '\"')
						pipeQuoting = true;  // quoting starts here
					else if (isspace(buffer[i]))  // all commands end with a space?
					{
						// process only after sure that quoted text is OK
						OnPipeCommand(msg);
						msg = &(buffer[i+1]);  // prepare next message, if present
					}  // end if
				}  // end if (pipeQuoting)

            }  // end for (i)
        }  // end if (control_pipe)
    }  // end if (theEvent)
    pipe_current_node = current_node;
    pipe_current_sprite = current_sprite;
    current_node = tmp_node;
    current_sprite = tmp_sprite;
    
}  // end SdtApp::OnControlMsg()

void SdtApp::FillWindow()
{
    int x, y;
	if (!map_canvas)
		return;

    for (int i=0;i<2;i++)   // must do it twice to get rid of scroll bars
    {
        map_canvas->GetClientSize(&x, &y);    
        SetBackgroundSize(x, y); 
    }

} //end SdtApp::FillWindow()

void SdtApp::AutoSize()
{
    int x, y;
    double x_ratio, y_ratio, aspect_ratio;
    
	if (!map_canvas)
		return;

    for (int i=0;i<2;i++)   // must do it twice to get rid of scroll bars
    {
        map_canvas->GetClientSize(&x, &y);
        
        aspect_ratio = (double)bg_imagewidth / (double)bg_imageheight;
        x_ratio = (double)bg_imagewidth / (double)x;
        y_ratio = (double)bg_imageheight / (double)y;
	
        if(x_ratio < y_ratio)
        {
            long new_x = (long)((double)y*aspect_ratio + 0.5);
            SetBackgroundSize(new_x, y);
        }
        else
        {
            long new_y = (long)((double)x/aspect_ratio + 0.5);
            SetBackgroundSize(x, new_y);
        }
    }
} //end SdtApp::AutoSize()


bool SdtApp::SetPopup(const char* title)
{
    SdtPopup* popup = (SdtPopup*)popup_list.FindFrameByTitle(title);
    if (popup)
    {
        current_popup = popup;
        return true;
    }
    else
    {
        if (!(popup = new SdtPopup(this)))
        {
            perror("SdtApp::SetPopup() error allocating frame");
            return false;   
        }
        if (popup->Create(title))
        {            
            popup_list.Prepend(popup);
            current_popup = popup;
            return true;
        }
        else
        {
            delete popup;
            fprintf(stderr, "SdtApp::SetPopup() error creating frame\n");
            return false;
        }     
    }    
}  // end SdtApp::SetPopup()

bool SdtApp::SetPopupContent(const char* content)
{
    if (current_popup)
    {
        return current_popup->SetContent(content);
    }
    else
    {
        fprintf(stderr, "SdtApp::SetPopupContent() no current popup\n");
        return false;
    }    
}  // end SdtApp::SetPopupContent()

bool SdtApp::ResizePopup()
{
    if (current_popup)
    {
        current_popup->Resize();
        return true;
    }
    else
    {
        fprintf(stderr, "SdtApp::ResizePopup() no current popup\n");
        return false;
    }
}  // end SdtApp::ResizePopup()

void SdtApp::DeletePopup(const char* title)
{
    SdtPopup* popup = (SdtPopup*)popup_list.FindFrameByTitle(title);
    if (popup) 
    {
        popup_list.Remove(popup);
        delete popup;
    }
}  // end SdtApp::DeletePopup()

bool SdtApp::OldSyntax(const char* colorLinkID,
                       const char* widthDirected)
{
	if (colorLinkID != NULL)
	{
		//if (!strcmp(colorLinkID, "X"))
		//	return true;

		// debug mode will report error if new syntax: link ID will
		// be an invalid color
		wxColour newColor(wxString::FromAscii(colorLinkID));
		if (newColor.Ok())
			return true;
	}

	if (widthDirected != NULL)
	{
		//if (!strcmp(widthDirected, "X"))
		//	return true;

		int lineWidth = atoi(widthDirected);
		if (lineWidth > 0)
			return true;  // no limit check, just confirm numeric
	}

	return false;  // none of the valid cases apply
}

bool SdtApp::MatchingLink(SdtNode* src, SdtNode* dst, 
                          DirType linkDirectedness,
                          SdtLink* currentLink)
{
	switch (linkDirectedness)
	{
	case BI_ONLY:
		if ((((src == currentLink->Src()) && (dst == currentLink->Dst())) ||
		    ((src == currentLink->Dst()) && (dst == currentLink->Src()))) &&
		    !currentLink->IsDirected() )
		{
			//fprintf(stderr, "MatchingLink - matching on BI_ONLY for link %s, node %s -> node %s\n",
			//        currentLink->GetLinkID(), src->GetName(), dst->GetName());
			return true;
		}
		break;

	case UNI_ONLY:
		if (((src == currentLink->Src()) && (dst == currentLink->Dst())) &&
		    currentLink->IsDirected())
		{
			//fprintf(stderr, "MatchingLink - matching on UNI_ONLY for link %s, node %s -> node %s\n",
			//        currentLink->GetLinkID(), src->GetName(), dst->GetName());
			return true;
		}
		break;

	case BI_AND_BOTH_UNI:
		if (((((src == currentLink->Src()) && (dst == currentLink->Dst())) ||
		      ((src == currentLink->Dst()) && (dst == currentLink->Src()))) &&
		      !currentLink->IsDirected()) ||
		    (((src == currentLink->Src()) && (dst == currentLink->Dst())) &&
             currentLink->IsDirected())   ||
		    (((src == currentLink->Dst()) && (dst == currentLink->Src())) &&
             currentLink->IsDirected()) )
		{
			//fprintf(stderr, "MatchingLink - matching on BI_AND_BOTH_UNI for link %s, node %s -> node %s\n",
			//        currentLink->GetLinkID(), src->GetName(), dst->GetName());
			return true;
		}
		break;

	case SAME_LINKID:
		if ((((((src == currentLink->Src()) && (dst == currentLink->Dst())) ||
		       ((src == currentLink->Dst()) && (dst == currentLink->Src()))) &&
		       !currentLink->IsDirected()) ||
		      (((src == currentLink->Src()) && (dst == currentLink->Dst())) &&
		        currentLink->IsDirected())   ||
		      (((src == currentLink->Dst()) && (dst == currentLink->Src())) &&
		        currentLink->IsDirected()) ) &&
		   !strcmp(currentLink->GetLinkID(), current_link_ID) )
		{
			//if (currentLink->IsDirected())
			//	fprintf(stderr, "MatchingLink - matching on SAME_LINKID for directed link (%s), node [%s] -> node [%s]\n",
			//	        currentLink->GetLinkID(), src->GetName(), dst->GetName());
			//else
			//	fprintf(stderr, "MatchingLink - matching on SAME_LINKID for undirected link (%s), node [%s] -> node [%s]\n",
			//	        currentLink->GetLinkID(), src->GetName(), dst->GetName());
			return true;
		}
		break;

	case NO_ALLS:
		//fprintf(stderr, "MatchingLink - matching NO_ALLS for link %s, node %s -> node %s\n",
		//        currentLink->GetLinkID(), currentLink->Src()->GetName(), currentLink->Dst()->GetName());
		break;
		
	default:
		//fprintf(stderr, "MatchingLink - no directedness for link %s, node %s -> node %s\n",
		//        currentLink->GetLinkID(), currentLink->Src()->GetName(), currentLink->Dst()->GetName());
		break;  // invalid value
	}

	return false;  // fall-through
}  // SdtApp::MatchingLink

////////////////////////////////////////////////////////
// SdtMapCanvas implementation

IMPLEMENT_DYNAMIC_CLASS(SdtMapCanvas, wxScrolledWindow)

BEGIN_EVENT_TABLE(SdtMapCanvas, wxScrolledWindow)
  EVT_PAINT         (SdtMapCanvas::OnPaint)
  EVT_MOUSE_EVENTS  (SdtMapCanvas::OnMouse)
END_EVENT_TABLE()


SdtMapCanvas::SdtMapCanvas(SdtMapListener* theListener)
 : last_y(0), listener(theListener), render_bitmap(NULL), printseqno(1)
{
}

SdtMapCanvas::~SdtMapCanvas()
{
    if (render_bitmap) delete render_bitmap;
}
  
bool SdtMapCanvas::Create(wxWindow* parent)
{
    wxPoint pos(0,0);
    wxSize size(INITIAL_WIDTH,INITIAL_HEIGHT);
    bool result = wxScrolledWindow::Create(parent, -1, pos, size,
                                      wxSUNKEN_BORDER | wxTAB_TRAVERSAL, 
                                      _T("map canvas"));
    if (result)
    {
        SetScrollRate(10, 10);
        SetBackgroundColour( wxT("BLUE") );
        if ( !SetSize(INITIAL_WIDTH,INITIAL_HEIGHT) )
	       return false;
        SetFocus();
    }
    
    return result;
}  // end SdtMapCanvas::Create()

bool SdtMapCanvas::SetSize(long width, long height)
{
    if (render_bitmap &&
        (width == render_bitmap->GetWidth()) &&  
        (height == render_bitmap->GetHeight())) 
        return true;
    
    wxBitmap* b = new wxBitmap(width, height);
    if (b)
    {
        if (render_bitmap) delete render_bitmap; 
        render_bitmap = b;
        SetVirtualSize(width, height);
        return true;
    }
    else
    {
        perror("SdtMapCanvas::SetSize() error allocating bitmap");
        return false;  
    }
}  // end SdtMapCanvas::SetSize()

void SdtMapCanvas::CenterOn(int x, int y)
// moves position (x,y, units of pixels) to center of visible screen
{
    int xscale, yscale;
    int origin_x, origin_y;     // new origin (upper-left corner) value in scroll units
    int offset_x, offset_y;     // origin-to-center offset values (pixels)
    
    GetScrollPixelsPerUnit(&xscale, &yscale);
    GetViewStart(&origin_x, &origin_y);
    GetClientSize(&offset_x, &offset_y);
    offset_x /= 2;
    offset_y /= 2;
    
    origin_x += (x-offset_x)/xscale;
    origin_y += (y-offset_y)/yscale;
    Scroll(origin_x, origin_y);
} // end SdtMapCanvas::CenterOn()

void SdtMapCanvas::ScreenCap()
{
    // print screen (without window attributes)
    int width, height, x, y, xscale, yscale;
    GetClientSize(&width, &height);
    if (width>0 && height>0)
    {
        width = (long)width<GetMapWidth() ? width : (int)GetMapWidth();
        height = (long)height<GetMapHeight() ? height : (int)GetMapHeight();
        GetScrollPixelsPerUnit(&xscale, &yscale);
        GetViewStart(&x, &y);
        x *= xscale;
        y *= yscale;
        
        wxImage* printImage = new wxImage(width, height);
        ASSERT(NULL != printImage);
        wxBitmap* printBitmap = new wxBitmap(*printImage);
		ASSERT(NULL != printBitmap);
        wxMemoryDC mdc, printdc;
        mdc.SelectObject(*render_bitmap);
        printdc.SelectObject(*printBitmap);
        printdc.Blit(0, 0, width, height, &mdc, x, y, wxCOPY);
        
        // get filename
        char filename[128];
        sprintf(filename, "sdt-%04d.png", printseqno++);
        /*wxString filename("sdt-");
        if (printseqno<10) filename.Append("000");
        else if (printseqno<100) filename.Append("00");
        else if (printseqno<1000) filename.Append("0");
        char
        filename.Append();
        filename.Append(".png");*/
        printBitmap->SaveFile(wxString::FromAscii(filename), wxBITMAP_TYPE_PNG);
        if (printBitmap) delete printBitmap;
    }
} // end SdtMapCanvas::ScreenCap()


void SdtMapCanvas::OnPaint(wxPaintEvent &WXUNUSED(event))
{    
    // Prepare a paint display context
    wxPaintDC dc(this);
    PrepareDC(dc);   
    //dc.BeginDrawing(); - Adamson 2.8 check
    // Draw our pre-rendered bitmap
    if (render_bitmap)
    {
        int width, height;
        GetClientSize(&width, &height);
        int xscale, yscale;
        GetScrollPixelsPerUnit(&xscale, &yscale);
        int x, y;
        GetViewStart(&x, &y);
        x *= xscale;
        y *= yscale;
        wxMemoryDC mdc;
        mdc.SelectObject(*render_bitmap);
        dc.Blit(0, 0, width, height, &mdc, x, y, wxCOPY);
        mdc.SelectObject(wxNullBitmap);
    }
    //dc.EndDrawing(); - Adamson 2.8 check
}  // end SdtMapCanvas::OnPaint()

void SdtMapCanvas::OnMouse(wxMouseEvent& event)
{   
    if(event.ButtonDClick() && !event.ControlDown() && !event.ShiftDown())        // double click
    {
        int xscale, yscale, x, y;
        GetScrollPixelsPerUnit(&xscale, &yscale);
	    GetViewStart(&x, &y);
    	
        // convert to pixels
        x*=xscale;
	    y*=yscale;
	    
        if (listener)
           listener->OnMapDoubleClick(event.GetX()+x, event.GetY()+y);
    }
	else if(event.ButtonDown() && event.ShiftDown())      // single click w/ shift
    {
        int xscale, yscale, x, y, max_x, max_y;
        GetScrollPixelsPerUnit(&xscale, &yscale);
	    GetViewStart(&x, &y);
		GetVirtualSize(&max_x, &max_y);
    	
        // convert to pixels
        x*=xscale;
	    y*=yscale;
	    
        if (listener)
           listener->OnMapShiftClick(event.GetX()+x, event.GetY()+y, max_x, max_y);
    }
    else if(event.ButtonDown() && event.ControlDown())      // single click w/ CTRL
    {
        CenterOn(event.GetX(), event.GetY());
    }
    else if (event.Dragging() && !event.ControlDown() && !event.ShiftDown())
    {
        long y = event.GetY();
        double scale = 1.0;
        if (y > last_y)
            scale *= 1.1;
        else if (y < last_y)
            scale /= 1.1;
        if (1.0 != scale)
        {
            long newWidth = (long)((double)GetMapWidth() * scale + 0.5);
            long newHeight = (long)((double)GetMapHeight() * scale + 0.5);
            if ((newWidth > 32) && (newHeight > 32)) 
                listener->OnMapResize(newWidth, newHeight);
            
        }
        last_y = y;
    } 
}  // end SdtMapCanvas::OnMouse()


////////////////////////////////////////////////////////
// SdtFrame implementation

IMPLEMENT_DYNAMIC_CLASS(SdtFrame, wxFrame)

BEGIN_EVENT_TABLE(SdtFrame, wxFrame)
  EVT_CLOSE         (SdtFrame::OnClose)
END_EVENT_TABLE()

SdtFrame::SdtFrame(SdtFrameListener* theListener)
 : listener(theListener), next(NULL)
{
}

SdtFrame::~SdtFrame()
{ 
    
}

void SdtFrame::OnClose(wxCloseEvent& /*event*/)
{
	if (listener) 
		listener->OnFrameDestruction(this);
	Destroy();
}

bool SdtFrame::Create(const char* title)
{
    wxString titleString = title ? wxString::FromAscii(title) : _T("Scripted Display Tool");
    if (wxFrame::Create((wxFrame*)NULL, -1, titleString))
    {
        SetClientSize(INITIAL_WIDTH, INITIAL_HEIGHT);
        return true;
    }
    else
    {
        fprintf(stderr, "SdtFrame::Create() error creating window\n");
        return false;   
    }
}  // end SdtFrame::Create()

////////////////////////////////////////////////////////
// SdtPopup implementation

IMPLEMENT_DYNAMIC_CLASS(SdtPopup, SdtFrame)
SdtPopup::SdtPopup(SdtFrameListener* theListener)
 : SdtFrame(theListener), show(false)
{
    
}

SdtPopup::~SdtPopup()
{
    
}

bool SdtPopup::Create(const char* title)
{
    if (SdtFrame::Create(title))
    {
        wxPoint pos(5,5);
        if (text.Create(this, -1, _T("info"), pos))
        {             
            // Specify a fixed-width font
            wxFont theFont(14, wxMODERN, wxNORMAL, wxNORMAL);
            text.SetFont(theFont);
            return true;
        }
        else
        {
           fprintf(stderr, "SdtPopup::Create() error creating text widget\n");
            return false;     
        }        
    }
    else
    {
        fprintf(stderr, "SdtPopup::Create() error creating window\n");
        return false;   
    }
}  // end SdtPopup::Create()

bool SdtPopup::SetContent(const char* content)
{
	if (!content || (content[0] == '\0') || (strlen(content) > CONTENT_MAX - 1))
		return false;

	int i = 0;
	int contentLength = strlen(content);
	char convertedNLs[CONTENT_MAX];

	strcpy(convertedNLs, content);  // TBD - limit-check

	// convert all occurrences of "\n" in content string to '\n' char
	while (i < contentLength + 1)  // include string terminator
	{
		if ((convertedNLs[i] == '\\') && (convertedNLs[i+1] == 'n'))
		{
			convertedNLs[i] = '\n';  // insert a newline
			// shift rest of text one char to the left
			for (int j = i + 1; j < contentLength + 1; j++)
				convertedNLs[j] = convertedNLs[j+1];

			contentLength--;
		}

		i++;  // point to next char to check
	}
    text.SetLabel(wxString::FromAscii(convertedNLs));
    int width, height;
    if (!show)
    {
        text.GetSize(&width, &height);
        SetClientSize(width+10, height+10);
        Show(true);
        show = true;   
    }
    Raise();
    return true;
}  // end SdtPopup::SetContent()

void SdtPopup::ResizeErrorPopup()
{
    // total hack until resize gets fixed
    int width, height;
    text.GetSize(&width, &height);
    SetClientSize((width+225), (height+50));
    Show(true);
    show = true;   
    Raise();
}  // end SdtPopup::RaiseErrorContent()

void SdtPopup::Resize()
{
    int width, height;
    text.GetSize(&width, &height);
    SetClientSize(width+10, height+10);
    Show(true);
    show = true;   
    Raise();
}  // end SdtPopup::Resize()
        
        
////////////////////////////////////////////////////////
// SdtSprite and SdtSpriteList implementation

SdtSprite::SdtSprite()
    : name(NULL), width(DEFAULT_WIDTH), height(DEFAULT_HEIGHT),
      prev(NULL), next(NULL)
{

}

SdtSprite::~SdtSprite()
{
    if (name) delete name;
}

bool SdtSprite::SetName(const char* spriteName)
{
	if (!spriteName)
		return false;

    char* ptr = new char[strlen(spriteName) + 1];
    if (!ptr)
		return false;

    strcpy(ptr, spriteName);
    if (name) delete name;
    name = ptr;
    return true;
}  // end SdtSprite::SetName()

bool SdtSprite::SetImage(char* imageFile)
{
    if (!imageFile) return false;

    bool loaded = false;
    unsigned int  numFrames = 0;
    // Test for animated GIF
    wxFileInputStream* imageStream = new wxFileInputStream(wxString::FromAscii(imageFile));
    if (imageStream)
    {
        wxGIFDecoder* gif = new wxGIFDecoder();//, true); - Adamson 2.8 check
        if (gif)
        {
            
            if (wxGIF_OK == gif->LoadGIF(*imageStream) &&
                (gif->IsAnimation()))
            {
                numFrames = (unsigned int)gif->GetFrameCount();
                if (!image_list.Init(numFrames))
                {
                    fprintf(stderr, "SdtSprite::SetImage() error initing image list\n");
                    delete gif;
                    delete imageStream;
                    return false;   
                    
                }
                bool loaded = true;
                for (unsigned int i = 0; i < numFrames; i++)
                {
                    if (!gif->ConvertToImage(i, image_list.GetImage(i)))
                    {
                        loaded = false;
                        image_list.Destroy();
                        break;
                    }
                }
            }
            delete gif;       
        }
        else
        {
            perror("SdtSprite::SetImage() error creating gif decoder");   
        }
        delete imageStream;
    }
    else
    {
        perror("SdtSprite::SetImage() error creating input stream");  
    }
       
    if (!loaded)
    {
        // Just load up one image
        image_list.Destroy();
        if (!image_list.Init(1))
        {
            fprintf(stderr, "SdtSprite::SetImage() error initing image list\n");
            return false;           
        }
        wxImage* image = image_list.GetImage(0);
        if (!image->LoadFile(wxString::FromAscii(imageFile)))
        {
            wxLogError(_T("SdtSprite::SetImage() Couldn't load image from file '%s'."), imageFile);
            return false;
        }
        numFrames = 1; 
    }
    
    // Scale image based on our current sprite size
    // (assumes all images in list (animation) are equal size)
    long imageWidth = image_list.GetImage(0)->GetWidth();
    long imageHeight = image_list.GetImage(0)->GetHeight();
    double scale = (width < height) ?
                   (double)width / (double)imageWidth :
                   (double)height / (double)imageHeight;
    width = (long)(scale * (double)imageWidth + 0.5);
    height = (long)(scale * (double)imageHeight + 0.5);
    
    if (!bitmap_list.Init(numFrames))
    {
        fprintf(stderr, "SdtSprite::SetImage() error initing bitmap list\n");
        image_list.Destroy();
        return false;   
    }
    for (unsigned int i = 0; i < numFrames; i++)
    {
        wxImage* image = image_list.GetImage(i);  
        wxBitmap* b = new wxBitmap(image->Scale(width, height));
        if (b)
        {
            bitmap_list.SetBitmap(i, b);
        }
        else
        {
            perror("SdtSprite::SetImage() error creating bitmap");
            image_list.Destroy();
            bitmap_list.Destroy();
            return false;
        }
    }
    return true;
}  // end SdtSprite::SetImage()

bool SdtSprite::SetSize(long theWidth, long theHeight)
{
    if ((theWidth == width) && (theHeight == height)) return true;
    unsigned int numFrames = image_list.GetImageCount();
    for (unsigned int i = 0; i < numFrames; i++)
    {
        wxImage* image = image_list.GetImage(i);   
        wxBitmap* b = new wxBitmap(image->Scale(theWidth, theHeight));
        if (b)
        {
            bitmap_list.SetBitmap(i, b);
        }
        else
        {
            perror("SdtSprite::SetSize() error allocating bitmap");
            bitmap_list.Destroy();
            return false;
        }
    }
    width = theWidth;
    height = theHeight;
    return true;
}  //end SdtSprite::SetSize()


SdtSpriteList::SdtSpriteList()
  : head(NULL), tail(NULL)
{
    
}

SdtSpriteList::~SdtSpriteList()
{
    Destroy();
}

void SdtSpriteList::Destroy()
{
    SdtSprite* next = head;
    while (next)
    {
        SdtSprite* current = next;
        next = next->next;
        delete current;   
    }   
    head = tail = NULL;
}  // end SdtSpriteList::Destroy()


void SdtSpriteList::Append(SdtSprite* sprite)
{
    if ((sprite->prev = tail))
        sprite->prev->next = sprite;
    else
        head = sprite;
    sprite->next = NULL;
    tail = sprite;
}   // end SdtSpriteList::Append()

void SdtSpriteList::Remove(SdtSprite* sprite)
{
    if (sprite->prev)
        sprite->prev->next = sprite->next;
    else
        head = sprite->next;
    if (sprite->next)
        sprite->next->prev = sprite->prev;
    else
        tail = sprite->prev;
}  // end SdtSpriteList::Remove()

SdtSprite* SdtSpriteList::FindSpriteByName(const char* name)
{
	if (!name || !strcmp(name, "none"))
		return NULL;

    SdtSprite* next = head;
    while (next)
    {
        if (!strcmp(name, next->GetName())) return next;
        next = next->next;
    }
	if (!strcmp(name,"default"))
		return head;

    return NULL;
}  // end SdtSpriteList::FindSpriteByName()

////////////////////////////////////////////////////////
// SdtNode and SdtNodeList implementation

SdtNode::SdtNode()
 : name(NULL), labeltext(NULL), pos_x(-1), pos_y(-1), sprite(NULL), 
   label(true), labelcolor(_T("yellow")), symbol(NO_SYMBOL), 
   symbolcolor(_T("red")), symbolthickness(1), circlexradius(0),
   circleyradius(0), index(0), prev(NULL), next(NULL)
{
    
}

SdtNode::~SdtNode()
{
    if (name) delete name;
    if (labeltext) delete labeltext;
}

bool SdtNode::SetName(const char* nodeName)
{
	if (!nodeName)
		return false;

    char* ptr = new char[strlen(nodeName) + 1];
    if (!ptr)
		return false;

    strcpy(ptr, nodeName);
    if (name) delete name;

    name = ptr;
    SetLabelText(name);
    return true;
}  // end SdtNode::SetName()

bool SdtNode::SetLabelText(const char* labelText)
{
	if (!labelText || !strcmp(labelText, "X"))
		return true;  // leave label text as is

	if (labeltext) delete labeltext;

    labeltext = new char[strlen(labelText) + 1];
	if (!labeltext)
	{
		fprintf(stderr, "SdtNode::SetLabelText() new labelText (len:%d) error: %s\n", (int)strlen(labelText), GetErrorString());
        return false;
	}

	strcpy(labeltext, labelText);
	return true;
}  // end SdtNode::SetLabelText()

bool SdtNode::IsAtPosition(long x, long y)
{
	// x,y should be in terms of pixels

	// get center positions in terms of pixels
	long center_x = wxGetApp().RemapX(pos_x);
	long center_y = wxGetApp().RemapY(pos_y);
	wxMemoryDC mdc;
	wxCoord tw=0, th=0;    // text width, height
    
	if (label)
	{
		// get text width, height for below
		mdc.GetTextExtent(wxString::FromAscii(labeltext), &tw, &th);
	}

	// check for image or image/text combination
	if (sprite)
	{
		long height = sprite->GetHeight();
		long width = sprite->GetWidth();

		//test for x,y inside image
		if (x < (center_x + width/2) && x > (center_x - width/2) &&
		    y < (center_y + height/2) && y > (center_y - height/2))
			return true;

		// check for x,y inside text with associated image
		if (label)
		{
			if (x < (center_x + (int)tw/2) &&
			    x > (center_x - (int)tw/2) &&
			    y < (center_y + height/2 + (int)th) &&
			    y > (center_y + height/2))
				return true;
		}
	}
	// else check for x,y inside label when no image
	else if (label && x < (center_x + (int)tw/2) &&
	                  x > (center_x - (int)tw/2) &&
	                  y < (center_y + (int)th/2) &&
	                  y > (center_y - (int)th/2))
		return true;

	// report not found
	return false;
}  // end SdtNode::IsAtPosition()

bool SdtNode::SetSymbol(const char* val)
{
	if (!val || !strcmp(val, "X"))
		return true;   // leave as is

    if (!strcmp(val, "circle") || !strcmp(val, "CIRCLE")
        || !strcmp(val,"sphere") || !strcmp(val,"SPHERE"))
        symbol = CIRCLE;
    else if (!strcmp(val, "ellipse") || !strcmp(val, "ELLIPSE"))
        symbol = ELLIPSE;
    else if (!strcmp(val, "rectangle") || !strcmp(val, "RECTANGLE")
             || !strcmp(val, "rect") || !strcmp(val, "RECT"))
        symbol = RECTANGLE;
    else if (!strcmp(val, "square") || !strcmp(val, "SQUARE")
             || !strcmp(val,"cube") || !strcmp(val,"CUBE"))             
         symbol = SQUARE;
    else if (!strcmp(val, "rnd_rectangle") || !strcmp(val, "RND_RECTANGLE")
             || !strcmp(val, "rndrectangle") || !strcmp(val, "RNDRECTANGLE")
             || !strcmp(val, "rrectangle") || !strcmp(val, "RRECTANGLE")
             || !strcmp(val, "rnd_rect") || !strcmp(val, "RND_RECT")
             || !strcmp(val, "rndrect") || !strcmp(val, "RNDRECT")
             || !strcmp(val, "rrect") || !strcmp(val, "RRECT"))
         symbol = RND_RECTANGLE;
    else if (!strcmp(val, "rnd_square") || !strcmp(val, "RND_SQUARE")
             || !strcmp(val, "rndsquare") || !strcmp(val, "RNDSQUARE")
             || !strcmp(val, "rsquare") || !strcmp(val, "RSQUARE"))
        symbol = RND_SQUARE;
    else if (!strcmp(val, "no_symbol") || !strcmp(val, "NO_SYMBOL")
             || !strcmp(val, "none") || !strcmp(val, "NONE")
             || !strcmp(val, "off") || !strcmp(val, "OFF"))
        symbol = NO_SYMBOL;
    else return false;
    return true;
}  // end SdtNode::SetSymbol()

const wxBitmap* SdtNode::GetBitmap(bool advance)
{
    if (!sprite) return NULL;
    unsigned int currentFrame = index++;
    if (advance)
    {
        if (index >= sprite->GetFrameCount()) index = 0;
    }
    return sprite->GetBitmap(currentFrame);   
}  // end SdtNode::GetBitmap()

SdtNodeList::SdtNodeList()
 : head(NULL), tail(NULL)
{
}

SdtNodeList::~SdtNodeList()
{
    Destroy();
}

void SdtNodeList::Destroy()
{
    SdtNode* next = head;
    while (next)
    {
        SdtNode* current = next;
        next = next->next;
        delete current;   
    }   
    head = tail = NULL;
}  // end SdtNodeList::Destroy()

void SdtNodeList::Prepend(SdtNode* node)
{
    if ((node->next = head))
        node->next->prev = node;
    else
        tail = node;
    node->prev = NULL;
    head = node;
}  // end SdtNodeList::Prepend()

void SdtNodeList::Append(SdtNode* node)
{
    if ((node->prev = tail))
        node->prev->next = node;
    else
        head = node;
    node->next = NULL;
    tail = node;
}   // end SdtNodeList::Append()

void SdtNodeList::Remove(SdtNode* node)
{
	// assumes that node is non-NULL
    if (node->prev)
        node->prev->next = node->next;
    else
        head = node->next;

    if (node->next)
        node->next->prev = node->prev;
    else
        tail = node->prev;
}  // end SdtNodeList::Remove()

void SdtNodeList::RemoveSprites()
{
	// loop through node list, remove all sprites
	SdtNode* nextNode = head;
	while (nextNode)
	{
		nextNode->SetSprite(NULL);
		nextNode = nextNode->next;
	}
}

void SdtNodeList::RemoveSymbols()
{
	// loop through node list, remove all symbols
	SdtNode* nextNode = head;
	while (nextNode)
	{
		nextNode->SetSymbol("off");
		nextNode = nextNode->next;
	}
}

SdtNode* SdtNodeList::FindNodeByName(const char* name)
{
	if (!name)
		return NULL;

    SdtNode* next = head;
    while (next)
    {
        if (!strcmp(name, next->GetName())) return next;
        next = next->next;
    }
    return NULL;
}

SdtNode* SdtNodeList::FindNodeByPosition(long x, long y)
{
    SdtNode* next = head;
    while (next)
    {
	    if (next->IsAtPosition(x, y)) 
            return next;
        else
            next = next->next;
    }
    return NULL;
}//end SdtNodeList::FindNodeByPosition()

////////////////////////////////////////////////////////
// SdtLink and SdtLinkList implementation

SdtLink::SdtLink(const SdtNode* srcNode, const SdtNode* dstNode,
                 const char* linkID, bool directedLink)
 : src(srcNode), dst(dstNode), color(_T("red")), thickness(1),
   prev(NULL), next(NULL), link_label_on(LABEL_CLEAR),
   link_label_color(_T("red")),
   prev_mult(NULL), next_mult(NULL),
   src_x_remapped(0), src_y_remapped(0),
   dst_x_remapped(0), dst_y_remapped(0),
   directed(directedLink), link_label_color_set(false)
{ 
	if (linkID == NULL)
		link_ID[0] = '\0';
	else
		strcpy(link_ID, linkID);  // TBD limit-check

	// make linklabel text default to an empty string
	link_label_text[0] = '\0';

}

SdtLink::~SdtLink()
{
}

bool SdtLink::SetLinkLabelColor(const char* labelColor)
{
	if ((labelColor == NULL) || !strcmp(labelColor, "X"))
		return true;  // keep previous value

	// set color of label text
	wxColour newColor(wxString::FromAscii(labelColor));
	bool retVal = newColor.Ok();
	if (retVal)
		link_label_color = newColor;  // change to new value

	link_label_color_set = true;
	return retVal;
}

bool SdtLink::SetLinkLabelText(const char* labelText)
{
	if (labelText == NULL)
	{
		link_label_on = LABEL_CLEAR;  // reset
		link_label_text[0] = '\0'; // zap whatever was present
	}
	else if (!strcmp(labelText, "X"))
		return true;  // keep previous text
	else
		strcpy(link_label_text, labelText);  // TBD - limit check?

	return true;
}

void SdtLink::SetLinkLabelTextDefault()
{
	// define linklabel default as "node1:node2:linkid"
	// LJT's 2010-03-29 email stated that the new default is just "linkID"
	//const char* srcName = const_cast<SdtNode*>(src)->GetName();
	//strcpy(link_label_text, srcName);
	//strcat(link_label_text, ":");

	//const char* dstName = const_cast<SdtNode*>(dst)->GetName();
	//strcat(link_label_text, dstName);

	if (link_ID[0] != '\0')
	{
		//strcat(link_label_text, ":");
		strcat(link_label_text, link_ID);
	}
	//fprintf(stderr, "SdtLink::SetLinkLabelTextDefault - setting to (%s)\n", link_label_text);
}

// counts number of links which are multiples to this one AFTER this one
int SdtLink::CountMults(SdtLink* headMult)
{
	SdtLink* nextLink = headMult->next_mult;  // start with next in list
	int multCount = 0;

	while (nextLink)
	{
		multCount++;
		nextLink = nextLink->next_mult;
	}

	//fprintf(stderr, "SdtLink::CountMults - counted %d multiple(s) after head\n", multCount);
	return multCount;
}

void SdtLink::CalcArcParams()
{
	// calculate midpoint, slope of perp. bisector, centerLineAngle
	double xMid = (wxGetApp().RemapX(dst->GetPosX()) +
	               wxGetApp().RemapX(src->GetPosX())) / 2;
	double yMid = (wxGetApp().RemapY(dst->GetPosY()) +
	               wxGetApp().RemapY(src->GetPosY())) / 2;

	double xDiff = wxGetApp().RemapX(dst->GetPosX()) -
	               wxGetApp().RemapX(src->GetPosX());
	double yDiff = wxGetApp().RemapY(dst->GetPosY()) -
	               wxGetApp().RemapY(src->GetPosY());

	double maxLinkRadius = 1.5 * sqrt(xDiff * xDiff +
	                                  yDiff * yDiff);

	//fprintf(stderr, "*** CalcArcParams - maxLinkRadius = %f, src %s (%ld, %ld), dst %s (%ld, %ld)\n",
	//        maxLinkRadius,
	//        src->GetName(), 
	//        wxGetApp().RemapX(src->GetPosX()), wxGetApp().RemapY(src->GetPosY()),
	//        dst->GetName(),
	//        wxGetApp().RemapX(dst->GetPosX()), wxGetApp().RemapY(dst->GetPosY()));

	// calculate vector angle to circle center
	double centerLineAngle = atan2(+xDiff , yDiff);  // range is (-PI, PI)

	double xMax = maxLinkRadius * cos(centerLineAngle);
	double yMax = -maxLinkRadius * sin(centerLineAngle);
	//fprintf(stderr, "CalcArcParams - centerLineAngle = %f deg, xDiff = %f, yDiff = %f, pb slope = %f, xMax = %f, yMax = %f\n",
	//        centerLineAngle * 180 / PI, xDiff, yDiff, xDiff / yDiff, xMax, yMax);

	x_center = xMid + xMax;  // center of arc in pixel space
	y_center = yMid + yMax;

	//fprintf(stderr, "CalcArcParams - distance from ctr (%f, %f) to src: %lf\n", x_center, y_center,
	//        sqrt((wxGetApp().RemapY(src->GetPosY()) - y_center) * (wxGetApp().RemapY(src->GetPosY()) - y_center) +
	//             (wxGetApp().RemapX(src->GetPosX()) - x_center) * (wxGetApp().RemapX(src->GetPosX()) - x_center)));

	//fprintf(stderr, "CalcArcParams - distance from ctr to dst: %lf\n",
	//        sqrt((wxGetApp().RemapY(dst->GetPosY()) - y_center) * (wxGetApp().RemapY(dst->GetPosY()) - y_center) +
	//             (wxGetApp().RemapX(dst->GetPosX()) - x_center) * (wxGetApp().RemapX(dst->GetPosX()) - x_center)));

	x_inc = .8 * xMax;  // will be modified based on # of links
	y_inc = .8 * yMax;

	//fprintf(stderr, "CalcArcParams - x_inc = %lf, y_inc = %lf\n", x_inc, y_inc);
}  // end SdtLink::CalcArcParams

long SdtLink::GetIncX(SdtLink* headMult)
{
	int numLinks = CountMults(headMult);
	//fprintf(stderr, "GetIncX - head = %s, this = %s, x_inc = %f, numLinks = %d\n", headMult->GetLinkID(), GetLinkID(), headMult->x_inc, numLinks);
	return static_cast<long>((headMult->x_inc / numLinks) + 0.5);
}

long SdtLink::GetIncY(SdtLink* headMult)
{
	int numLinks = CountMults(headMult);
	//fprintf(stderr, "GetIncY - y_inc = %f, numLinks = %d\n", headMult->y_inc, numLinks);
	return static_cast<long>((headMult->y_inc / numLinks) + 0.5);
}

void SdtLink::RemapNodeSwap()
{
	src_x_remapped = wxGetApp().RemapX(src->GetPosX());
	src_y_remapped = wxGetApp().RemapY(src->GetPosY());
	dst_x_remapped = wxGetApp().RemapX(dst->GetPosX());
	dst_y_remapped = wxGetApp().RemapY(dst->GetPosY());
	//fprintf(stderr, "RemapNodeSwap - remapped src: (%ld, %ld), remapped dst: (%ld, %ld)\n",
	//        src_x_remapped, src_y_remapped, dst_x_remapped, dst_y_remapped);

	// determine angle from src to dst in counter-clockwise direction
	double angleSrc = atan2(-(wxGetApp().RemapY(src->GetPosY()) - y_center),
	                          wxGetApp().RemapX(src->GetPosX()) - x_center);
	double angleDst = atan2(-(wxGetApp().RemapY(dst->GetPosY()) - y_center),
	                          wxGetApp().RemapX(dst->GetPosX()) - x_center);
	//fprintf(stderr, "RemapNodeSwap - center of arc = (%f, %f), angleSrc = %f deg, angleDst = %f deg, link (%s)\n",
	//        x_center, y_center, angleSrc * 180 / PI, angleDst * 180 / PI, GetLinkID());

	// swap nodes if dst node is not "in front of" src (counterclockwise)
	if ( !(((angleSrc >= 0) && ((angleDst > angleSrc) ||
	                          (angleDst < (angleSrc - PI)) )) ||
	       ((angleSrc < 0) && (((angleDst <= 0) && (angleDst > angleSrc)) ||
	                         ((angleDst > 0)  && (angleDst < (angleSrc + PI)))))) )
	{
		//fprintf(stderr, "    swapping nodes\n");
		long tempRemap = src_x_remapped;  // yes, so swap
		src_x_remapped = dst_x_remapped;
		dst_x_remapped = tempRemap;

		tempRemap = src_y_remapped;
		src_y_remapped = dst_y_remapped;
		dst_y_remapped = tempRemap;
	}
}

bool SdtLink::Match(const SdtNode* theSrc, const SdtNode* theDst,
                    const char* linkID, bool directedLink) const
{
	if (!theSrc || !theDst)
		return false;

	if ( ((src == theSrc) && (dst == theDst)) ||
	     ((src == theDst) && (dst == theSrc)) )
	{
		if ((((linkID == NULL) && (link_ID[0] == '\0')) ||
		     ((linkID != NULL) && strcmp(linkID, link_ID) == 0)) &&
		     !directed && !directedLink)
		{
			//fprintf(stderr, "SdtLink::Match - found a match for linkID %s\n", linkID);
			return true;  // there is a match
		}
	}

	// check for matching directed links
	if ((src == theSrc) && (dst == theDst))
	{
		if ((((linkID == NULL) && (link_ID[0] == '\0')) ||
		     ((linkID != NULL) && strcmp(linkID, link_ID) == 0)) &&
		     directed && directedLink)
		{
			//fprintf(stderr, "SdtLink::Match - found a match for (%s, %s, linkID: %s, (directed))\n",
			//        theSrc->GetName(), theDst->GetName(), linkID);
			return true;
		}
	}

	return false;
}

bool SdtLink::SetLinkLabel(const char* offOnColor, const char* labelText)
{
	bool labelTextOK = SetLinkLabelText(labelText);  // set if present
	bool oocOK = true;

	if (!link_label_color_set)
		link_label_color = color;  // default to link's color

	if (!strcmp(offOnColor, "off") || !strcmp(offOnColor, "OFF"))
	{
		SetLinkLabelOff();
	}
	else if (!strcmp(offOnColor, "on") || !strcmp(offOnColor, "ON"))
	{
		// if "on" but no previous text defined, then assign default
		if (GetLinkLabelText() == NULL)
			SetLinkLabelTextDefault();

		SetLinkLabelOn();
	}
	else  // set color
	{
		//fprintf(stderr, "SetLinkLabel - setting color to %s\n", offOnColor);
		// reports error if invalid color
		oocOK = SetLinkLabelColor(offOnColor);  // takes care of "X" option
	}

	return (labelTextOK && oocOK);
}

SdtLinkList::SdtLinkList()
 : head(NULL), tail(NULL)
{
}

SdtLinkList::~SdtLinkList()
{
    Destroy();
}

void SdtLinkList::Destroy()
{
    SdtLink* next = head;
    while (next)
    {
        SdtLink* current = next;
        next = next->next;
        delete current;
    }
    head = tail = NULL;
}  // end SdtLinkList::Destroy()

void SdtLinkList::Append(SdtLink* link)
{
    if ((link->prev = tail))  // link already NULL-checked
        link->prev->next = link;
    else
        head = link;

    link->next = NULL;
    tail = link;

}  // end SdtLinkList::Append()

void SdtLinkList::Remove(SdtLink* link)
{
	// assumes that link is non-NULL
    if (link->next)
        link->next->prev = link->prev;
    else
        tail = link->prev;

    if (link->prev)
        link->prev->next = link->next;
    else
        head = link->next;   

	// remove from "multiple link" list, if present
	if (link->next_mult)
		link->next_mult->prev_mult = link->prev_mult;

	if (link->prev_mult)
		link->prev_mult->next_mult = link->next_mult;

	//fprintf(stderr, "SdtLinkList::Remove - Remove-ing (mult) link(%s) if present\n", link->GetLinkID());
	//SdtLink* nextLink = HeadMultipleLink(link);  // get head if it exists
	//fprintf(stderr, "SdtLinkList::Remove - remaining link(s):\n");
	//while (nextLink)
	//{
	//	fprintf(stderr, "   (%s)\n", nextLink->GetLinkID());
	//	nextLink = nextLink->next_mult;
	//}
}  // end SdtLinkList::Remove()

SdtLink* SdtLinkList::FindLink(const SdtNode* src, const SdtNode* dst,
                               const char* linkID,
                               const char* dir)
{
	if (!src || !dst)
		return NULL;

	// also takes into account ",all,all" (obsolete?)
	if (linkID && !strcmp(linkID, "all"))
		return NULL;  // 

    SdtLink* next = head;
    while (next)
    {
		bool directed = false;
		if (dir && !strcmp(dir, "dir"))
			directed = true;

        if (next->Match(src, dst, linkID, directed))
			break;

        next = next->next;
    }

    return next;  // NULL if fall through loop
}  // end SdtLinkList::FindLink()

SdtLink* SdtLinkList::AddLink(const SdtNode* src, const SdtNode* dst,
                              const char* linkID,
                              const char* dir)
{
	if (!src || !dst)
		return NULL;

	// ",all,*" checking has already taken place

	// does link already exist?
	SdtLink* link = FindLink(src, dst, linkID, dir);
	if (link != NULL)
	{
		return link;  // yes
	}

	bool directed = false;
	if (dir && !strcmp(dir, "dir"))
		directed = true;

	if ((link = new SdtLink(src, dst, linkID, directed)) == NULL)
	{
		perror("SdtLinkList::AddLink() memory allocation error:");
		return NULL;
	}

	Append(link);  // add to list

	SdtLink* headMult = HeadMultipleLink(link);
	if (headMult != NULL)  // already exist a link between same nodes?
	{
		AddToMultList(headMult, link);   // yes, add this one to list
	}

	return link;
}  // end SdtLinkList::AddLink()

bool SdtLinkList::RemoveLink(const SdtNode* src, const SdtNode* dst,
                             const char* linkID,
                             const char* dir)
{
	if (!src || !dst)
		return false;

    SdtLink* link = FindLink(src, dst, linkID, dir);
    if (!link)
		return false;

	Remove(link);
	delete link;
	return true;
}  // end SdtLinkList::RemoveLink()

void SdtLinkList::RemoveLinks(const SdtNode* node)
{
    SdtLink* next = head;
    while (next)
    {
        SdtLink* current = next;
        next = next->next;
        if ((node == current->src) || (node == current->dst))
        {
            Remove(current);
            delete current;
        }
    }
}  // end SdtLinkList::RemoveLinks()

void SdtLinkList::RemoveLinkLabels()
{
	SdtLink* nextLabel = head;
	while (nextLabel)
	{
		nextLabel->SetLinkLabelText(NULL);
		nextLabel = nextLabel->next;
	}
}

SdtLink* SdtLinkList::HeadMultipleLink(SdtLink* newLink)
{
	//fprintf(stderr, "SdtLinkList::HeadMultipleLink - entering for link (%s)\n", newLink->GetLinkID());
	SdtLink* nextLink = head;
	while (nextLink)
	{
		if ( ( ((newLink->src == nextLink->src) &&
			    (newLink->dst == nextLink->dst)) ||
		       ((newLink->src == nextLink->dst) &&
			    (newLink->dst == nextLink->src)) ) &&
		     ( (strcmp(newLink->GetLinkID(), nextLink->GetLinkID()) != 0) ||
			   (newLink->IsDirected() && nextLink->IsDirected() && (newLink != nextLink)) ) &&
		     (nextLink->prev_mult == NULL) )
		{
			//fprintf(stderr, "SdtLinkList::HeadMultipleLink - head of multiple links: src (%s), dst (%s), ID (%s)\n",
			//        const_cast<SdtNode*>(nextLink->src)->GetName(), const_cast<SdtNode*>(nextLink->dst)->GetName(), nextLink->GetLinkID());
			// align all multiple nodes so that arcs are correct
			//newLink->src = nextLink->src;
			//newLink->dst = nextLink->dst;
			break;  // nextLink is head of links connecting same 2 nodes
		}

		nextLink = nextLink->next;
	}

	return nextLink;  // NULL if no head link found
}

void SdtLinkList::AddToMultList(SdtLink* headMult, SdtLink* newLink)
{
	// both inputs assumed to be non-NULL
	if (!headMult || !newLink)
		return;

 	SdtLink* multLink = headMult;
	SdtLink* lastLink = NULL;

	// assumes that the link with no ID, if present,
	// will end up as the head link
	while (multLink)
	{
		//fprintf(stderr, "SdtLinkList::AddToMultList - comparing (%s) to (%s)\n", newLink->GetLinkID(), multLink->GetLinkID());
		if (strcmp(newLink->GetLinkID(), multLink->GetLinkID()) < 0)
		{
			//fprintf(stderr, "SdtLinkList::AddToMultList - newLink (%s) < multLink (%s), inserting...\n", newLink->GetLinkID(), multLink->GetLinkID());
			newLink->next_mult = multLink;  // insert into mult lnk list
			newLink->prev_mult = multLink->prev_mult;  // could be NULL
			multLink->prev_mult = newLink;
			if (newLink->prev_mult != NULL)
				newLink->prev_mult->next_mult = newLink;

			break;  // done with insertion
		}

		lastLink = multLink;
		multLink = multLink->next_mult;
	}

	if (!multLink)  // went through whole list?
	{
		//fprintf(stderr, "SdtLinkList::AddToMultList - appending (%s, %s, ID: %s) to end of list (%s, %s, ID: %s)\n",
		//        newLink->Src()->GetName(), newLink->Dst()->GetName(), newLink->GetLinkID(),
		//        lastLink->Src()->GetName(), lastLink->Dst()->GetName(), lastLink->GetLinkID());
		newLink->prev_mult = lastLink;  // yes, so tack onto end
		lastLink->next_mult = newLink;
	}
}

SdtInputStack::SdtInputStack()
    : head(NULL)
{
}

SdtInputStack::~SdtInputStack()
{
    Item* next = head;
    while (next)
    {
        Item* current = next;
        next = next->next;
        delete current;     
    }  
    head = NULL; 
}  

bool SdtInputStack::AddLast(ProtoDispatcher::Descriptor theFile)
{
    Item* item = new Item(theFile);
    Item* last = head;
    if (item)
    {
        if (head)
        {
            Item* next = head;
            while (next)
            {
                last = next;
                next = next->next;
            }
            if (last)
              last->next = item;
        }
        else
          head = item;

        return true;
    }
    else
    {
        perror("SdtInputStack::AddLast() error allocating input item");
        return false;   
    }   
}  // end SdtInputStack::AddLast()

bool SdtInputStack::Push(ProtoDispatcher::Descriptor theFile)
{
    Item* item = new Item(theFile);
    if (item)
    {
        item->next = head;
        head = item;
        return true;
    }
    else
    {
        perror("SdtInputStack::Push() error allocating input item");
        return false;   
    }   
}  // end SdtInputStack::Push()


ProtoDispatcher::Descriptor SdtInputStack::Pop()
{
    if (head)
    {
        Item* item = head;
        head = head->next;
        ProtoDispatcher::Descriptor file = item->file;
        delete item;
        return file;
    }
    else
    {
        return ProtoDispatcher::INVALID_DESCRIPTOR;   
    }   
}  // end SdtInputStack::Pop()

SdtInputStack::Item::Item(ProtoDispatcher::Descriptor theFile)
 : file(theFile), next(NULL)
{
}

SdtImageList::SdtImageList()
 : count(0), array(NULL)
{
    
}

SdtImageList::~SdtImageList()
{
    Destroy();  // deletes array
}

bool SdtImageList::Init(unsigned int theCount)
{
    if (array) delete[] array;
    array = new wxImage[theCount];
    if (array)
    {
        count = theCount;
        return true;
    }
    else
    {
        perror("SdtImageList::Create() error allocating image array");
        return false;
    }       
}  // end SdtImageList::Init()

void SdtImageList::Destroy()
{
    for (unsigned int i = 0; i < count; i++)
    {
        array[i].Destroy();   
    }
    if (array)
    {    
        delete[] array;
        array = NULL;
    }
    count = 0;
}  // end SdtImageList::Destroy()

SdtBitmapList::SdtBitmapList()
 : count(0), array(NULL)
{
    
}

SdtBitmapList::~SdtBitmapList()
{
    Destroy();
}

bool SdtBitmapList::Init(unsigned int theCount)
{
    if (array) delete[] array;
    array = new wxBitmap*[theCount];
    if (array)
    {
        memset(array, 0, theCount*sizeof(wxBitmap*));
        count = theCount;
        return true;
    }
    else
    {
        perror("SdtBitmapList::Create() error allocating bitmap array");
        return false;
    }       
}  // end SdtBitmapList::Init()

void SdtBitmapList::Destroy()
{
    for (unsigned int i = 0; i < count; i++)
    {
        delete array[i];
        array[i] = (wxBitmap*)NULL;  
    }
    if (array)
    {    
        delete[] array;
        array = NULL;
    }
    count = 0;
}  // end SdtBitmapList::Destroy()

bool SdtBitmapList::SetBitmap(unsigned int index, wxBitmap* bitmap)
{
    if (index < count)
    {
        if (array[index]) delete array[index];
        array[index] = bitmap;
        return true;
    }
    else
    {
        fprintf(stderr, "SdtBitmapList::SetBitmap() error: invalid index\n");
        return false;    
    }       
}

SdtFrameList::SdtFrameList()
 : head(NULL)
{
    
}


SdtFrameList::~SdtFrameList()
{
    Destroy();
}

void SdtFrameList::Destroy()
{
    while (head)
    {
        SdtFrame* item = Remove(head);
        delete item;
    }   
}  // end SdtFrameList::Destroy()

SdtFrame* SdtFrameList::Remove(SdtFrame* frame)
{
	SdtFrame* prev = NULL;
    SdtFrame* next = head;

	if (!frame)
		return NULL;

    while (next)
    {
        if (frame == next)  // is this the one to delete?
        {
            if (prev)
                prev->next = frame->next;
            else
                head = frame->next;
            return frame;
        }
        else  // no, so set up try for next one
        {
            prev = next;
            next = next->next;
        }   
    }
    return NULL;  // could not find it
}  // end SdtFrameList::Remove()

SdtFrame* SdtFrameList::FindFrameByTitle(const char* title)
{
	if (!title)
		return NULL;

    SdtFrame* prev = NULL;
    SdtFrame* next = head;
    while (next)
    {
        if (!strcmp(title, (const char*)next->GetTitle().mb_str()))
        {
            return next;
        }
        else
        {
            prev = next;
            next = next->next;
        }   
    }
    return NULL;
}  // end SdtFrameList::FindFrameByTitle()


void SdtApp::GetInputCallBack(ProtoDispatcher::Descriptor descriptor, ProtoDispatcher::Event theEvent, const void* userData)
{
    SdtApp* ptr = (SdtApp*) userData;
    if (!( ptr->GetInput() )) ptr->GetNextFile();
}   // end SdtApp::GetInputCallBack()


//************************** 
// If EOF get next file:   *
//**************************
void SdtApp::GetNextFile()
{
    ProtoDispatcher::Descriptor    new_input;

    if (ProtoDispatcher::INVALID_DESCRIPTOR != current_input)
    {
        new_input = input_stack.Pop();
        if (ProtoDispatcher::INVALID_DESCRIPTOR != new_input)
        {
            dispatcher.RemoveGenericInput(current_input);
            current_input = new_input;
            dispatcher.InstallGenericInput(current_input, &SdtApp::GetInputCallBack,this);
        }
        else
        {
            dispatcher.RemoveGenericInput(current_input);
            current_input = ProtoDispatcher::INVALID_DESCRIPTOR;
        }
    }
} // end SdtApp::GetNextFile()


bool SdtApp::OnWaitTimeout(ProtoTimer& /*theTimer*/)
{
    if (!dispatcher.InstallGenericInput(current_input, &SdtApp::GetInputCallBack,this))
    {
        fprintf(stderr,"Error: failed to install file to dispatcher.\n");
    }

    return true;
}

bool SdtApp::SetLine(const char *lineColor, const char* lineWidth)  // will set color and thickness
{
	// line <color>[,<thickness>]
	bool colorOK = false;
	bool thicknessOK = false;
	if (current_link)
	{
		colorOK = current_link->SetColor(lineColor);
		thicknessOK = current_link->SetThickness(lineWidth);
		return (colorOK && thicknessOK);
	}

	if (!current_src || !current_dst)
		return false;  // cannot be an "all,*" option

	SdtLink* nextLink = link_list.Head();
	while (nextLink)
	{
		if (MatchingLink(current_src, current_dst, current_directedness,
		                 nextLink))
		{
			colorOK = nextLink->SetColor(lineColor);
			thicknessOK = nextLink->SetThickness(lineWidth);
		}
		nextLink = nextLink->Next();
	}

	return (colorOK && thicknessOK);  // only report on last setting
}

bool SdtApp::SetLinkLabel(const char* offOnColorText)
{
	if (!offOnColorText)
		return true;  // leave everything as is

	// linklabel off|on|<color>[,text]
	char ooctCopy[CMD_LINE_MAX];
	strcpy(ooctCopy, offOnColorText);

	char* offOnColor = strtok(ooctCopy, ",");  // "off", "on", or <color>
	char* labelText = strtok(NULL, ",");

	if (current_link != NULL)
	{
		return current_link->SetLinkLabel(offOnColor, labelText);
	}

	if (!current_src || !current_dst)
		return false;  // not a single option, not an "all" option

	bool labelOK = true;  // default

	SdtLink* nextLink = link_list.Head();
	while (nextLink)
	{
		if (MatchingLink(current_src, current_dst, current_directedness,
		                 nextLink))
		{
			labelOK = nextLink->SetLinkLabel(offOnColor, labelText);
		}
		nextLink = nextLink->Next();
	}

	return labelOK;  // reports only on last one
}

bool SdtApp::SetNodeLabel(const char* offOnColorText)
{
	// label off|on|<color>[,text]
	bool oocOK = true;  // default for "off" and "on"
	char ooctCopy[CMD_LINE_MAX];

	if (!offOnColorText)
		return true;  // leave node label as is

	strcpy(ooctCopy, offOnColorText);  // TBD - limit-check

	char* offOnColor = strtok(ooctCopy, ",");
	char* labelText = strtok(NULL, ",");

	if (current_node == NULL)
	{
		fprintf(stderr, "SdtApp::SetNodeLabel - no current node exists\n");
		return false;
	}

	if (!strcmp(offOnColor, "off") || !strcmp(offOnColor, "OFF"))
		current_node->SetLabel(false);
	else if (!strcmp(offOnColor, "on") || !strcmp(offOnColor, "ON"))
		current_node->SetLabel(true);
	else  // set color if present
	{
		// reports error if invalid color
		//fprintf(stderr, "SetNodeLabel - setting color to %s\n", offOnColor);
		oocOK = current_node->SetLabelColor(offOnColor);
	}

	bool labelTextOK = current_node->SetLabelText(labelText);
	return (oocOK && labelTextOK);
}

bool SdtApp::HasDblCommaLinkID(char* dcCopy)
{
	char* endSrc = strchr(dcCopy, ',');
	if (!endSrc)
		return false;  // should not happen

	char* endDst = strchr(endSrc+1, ',');
	if (!endDst)
		return false;  // should not happen

	char* endLinkID = strchr(endDst+1, ',');
	if (!endLinkID)
		return false;  // could happen

	if (endLinkID != endDst+1)
		return false;  // there was a non-NULL linkID

	//fprintf(stderr, "HasDblCommaLinkID - found a double comma\n");
	return true;  // double comma NULL linkID found
}
