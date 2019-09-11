 
#include "protokit.h"
#include <stdio.h>  // for stderr output as needed
#include <stdlib.h> // for atoi()

#define PIPE_TYPE ProtoPipe::MESSAGE

/**
 * @class SdtCmd
 *
 * @brief This program uses the ProtoPipe class to send
 * local domain interprocess communications to the sdt/sdt3d apps
 */
class SdtCmd : public ProtoApp
{
    public:
        SdtCmd();
        ~SdtCmd();

        // Overrides from ProtoApp 
        bool OnStartup(int argc, const char*const* argv);
        bool ProcessCommands(int argc, const char*const* argv);
        void OnShutdown();
        
        bool OnCommand(const char* cmd, const char* val);
            
    private:
        enum CmdType {CMD_INVALID, CMD_ARG, CMD_NOARG};
        static CmdType GetCmdType(const char* string);
        static const char* const CMD_LIST[];        
        void Usage();
        ProtoPipe    client_pipe;
        char         pipe_name[512];
            
};  // end class SdtCmd

// Instantiate our application instance 
PROTO_INSTANTIATE_APP(SdtCmd) 
        
SdtCmd::SdtCmd()
 : client_pipe(PIPE_TYPE)
{
  strcpy(pipe_name,"sdt");
}

SdtCmd::~SdtCmd()
{

}

const char* const SdtCmd::CMD_LIST[] =
{
    "+send",       // Send UDP packets to destination host/port
    "+instance",   // override sdt pipe name
    NULL
};
    
void SdtCmd::Usage()
{
    fprintf(stderr, "SdtCmd [instance <sdtPipName>] <sdt command>\n");
}  // end SdtCmd::Usage()
  
bool SdtCmd::OnStartup(int argc, const char*const* argv)
{
  if (client_pipe.IsOpen()) client_pipe.Close();
  if (!client_pipe.Connect(pipe_name))
    {
      PLOG(PL_ERROR, "SdtCmd::OnCommand() client_pipe.Connect() %s warning: %s.  Please supply a valid sdt pipe name.\n",
	  pipe_name,GetErrorString());
      return false;   
    }
  
  if (!ProcessCommands(argc, argv))
    {
      PLOG(PL_ERROR, "SdtCmd::OnStartup() error processing command line\n");
      Usage();
      return false;
    }
  return true;
}  // end SdtCmd::OnStartup()

void SdtCmd::OnShutdown()
{
  if (client_pipe.IsOpen()) client_pipe.Close();
  PLOG(PL_ERROR, "SdtCmd: Done.\n");
}  // end SdtCmd::OnShutdown()

bool SdtCmd::ProcessCommands(int argc, const char*const* argv)
{  
  // Dispatch command-line commands to our OnCommand() method
  int i = 1;
  while ( i < argc)
  {
    if (!strcmp(argv[i],"instance"))
	{
	  if (client_pipe.IsOpen()) client_pipe.Close();
	  if (!client_pipe.Connect(argv[i+1]))
	    {
	      PLOG(PL_ERROR, "SdtCmd::OnCommand() client_pipe.Connect() %s error: %s.  Please supply a valid sdt pipe name.\n",
		   pipe_name,GetErrorString());
	      return false;   
	    }
	  i++;
	}
    else 
	{
	  if (!client_pipe.IsOpen()) 
	    { 
	      PLOG(PL_ERROR,"SdtCmd::OnCommand() client pipe not open!\n");
	      return false;
	    }
	  if (!OnCommand("send", argv[i]))
	    {
	      PLOG(PL_ERROR, "SdtCmd::ProcessCommands() ProcessCommand(%s, %s) error\n", 
		   argv[i], argv[i+1]);
	      return false;
	    }
	}
    i++;
  }
  return true;
  
}  // end SdtCmd::ProcessCommands()

bool SdtCmd::OnCommand(const char* cmd, const char* val)
{
  if (!val)
    {
      PLOG(PL_ERROR, "SdtCmd::ProcessCommand missing argument\n");
      return false;
    }

  // Add room for trailing space & null terminating char
    unsigned int msg_len = strlen(val) + 2; 
	char * work_buffer;
    if (!(work_buffer = new char[msg_len]))
    {
      PLOG(PL_ERROR, "SdtCmd: new msg_buffer error: %s\n", GetErrorString());
      msg_len = 0;
      return false;  
    }
    sprintf(work_buffer,"%s",val);
  
    // Readd the "quotes" if necessary
    char* hasSpace = strchr(work_buffer,' ');
    if (hasSpace != NULL)
    {
        if (work_buffer) delete[] work_buffer;
        msg_len = strlen(val) + 4; // need space for quotes 
        if (!(work_buffer = new char[msg_len]))
	    {
	       PLOG(PL_ERROR,"SdtCmd: new msg_buffer error: %s\n",GetErrorString());
	       msg_len = 0;
	       return false;
	    }
      sprintf(work_buffer,"\"%s\"",val); 
    }

	// finally add trailing space
	char * msg_buffer = new char[msg_len];
	sprintf(msg_buffer,"%s ",work_buffer);
	
    if (!client_pipe.Send(msg_buffer, msg_len))
	    PLOG(PL_ERROR, "SdtCmd::OnSendTimeout() client_pipe.Send() error\n");  
  
    return true;
}  // end SdtCmd::OnCommand()


