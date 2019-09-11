#ifndef _SDT
#define _SDT

#include "wxProtoApp.h"

const int CMD_LINE_MAX = 256;
const int CONTENT_MAX = 1024;
const int FILE_NAME_MAX = 1024;  // must always be < PATH_NAME_MAX
const int PATH_NAME_MAX = 8192;
const int PIPE_MAX = 512;
const int PIPE_NAME_MAX = 512;
const int SOCKET_MAX = 8192;
const int STATUS_MAX = 512;

#ifdef WIN32
const char SLASH = '\\';
const char* PATHSEPS = ";";
const char* FILESEP = "\\";
#else
const char SLASH = '/';
const char* PATHSEPS = ":;";
const char* FILESEP = "/";
#endif

const double PI = 3.141592653589793;

class SdtImageList
{
    public:
        SdtImageList();
        ~SdtImageList();
        bool Init(unsigned int maxCount);
        void Destroy();
        
        unsigned GetImageCount() const {return count;}
        wxImage* GetImage(unsigned int index)
            {return (index < count) ? array+index : NULL;}
        
    private:
        unsigned int    count;
        wxImage*        array;       
};  // end class SdtImageList

class SdtBitmapList
{
    public:
        SdtBitmapList();
        ~SdtBitmapList();
        bool Init(unsigned int maxCount);
        void Destroy();
        
        unsigned GetBitmapCount() const {return count;}
        bool SetBitmap(unsigned int index, wxBitmap* bitmap);
        wxBitmap* GetBitmap(unsigned int index) const
            {return (index < count) ? array[index] : NULL;}
        
    private:
        unsigned int    count;
        wxBitmap**      array;   
};  // end class SdtBitmapList

class SdtSprite
{
    friend class SdtSpriteList;
    
    public:
         SdtSprite();
        ~SdtSprite();
        
        enum
        {
            DEFAULT_WIDTH = 32,
            DEFAULT_HEIGHT = 32
        };
        
        bool SetName(const char* spriteName);
        bool SetImage(char* imageFile);
        unsigned int GetFrameCount() const {return bitmap_list.GetBitmapCount();}
        bool SetSize(long theWidth, long theHeight);
        bool Scale(double factor)
        {
            long newWidth = (long)((double)width * factor + 0.5);
            long newHeight = (long)((double)height * factor + 0.5);
            return SetSize(newWidth, newHeight);
        }
        
        const char* GetName() const {return name;}
        wxBitmap* GetBitmap(unsigned int index = 0) const 
            {return bitmap_list.GetBitmap(index);}
        long GetWidth() const {return width;}
        long GetHeight() const {return height;}
        
        SdtSprite* Next() {return next;}
        
    private:
        char*           name;
        
        SdtImageList     image_list;
        SdtBitmapList    bitmap_list;
        long            width;
        long            height;
    
        SdtSprite*       prev;
        SdtSprite*       next;
};  // end class SdtSprite

class SdtSpriteList
{
    public:
        SdtSpriteList();
        ~SdtSpriteList();
        void Destroy();
        
        void Append(SdtSprite* sprite);
        void Remove(SdtSprite* sprite);
        SdtSprite* FindSpriteByName(const char* name);
        SdtSprite* Head() {return head;}
            
    private:
        SdtSprite* head;
        SdtSprite* tail;
};  // end class SdtSpriteList

class SdtNode
{
    friend class SdtNodeList;
    
    public:
        SdtNode();
        ~SdtNode();       
        bool SetName(const char* nodeName);
        bool SetLabelText(const char* labelText);
        void SetSprite(SdtSprite* theSprite) {sprite = theSprite;}
        void SetPosition(double x, double y)
        {
            pos_x = x;
            pos_y = y;
        }
        void SetLabel(bool state) {label = state;}
        
        const char* GetName() const {return name;}
        const char* GetLabelText() {return labeltext;}
        const wxBitmap* GetBitmap(bool advance);
        double GetPosX() const {return pos_x;}
        double GetPosY() const {return pos_y;}
        bool Label() {return label;}
	    bool IsAtPosition(long x, long y);
        bool SetLabelColor(const char* theColor)
        {
			if (!theColor || !strcmp(theColor, "X"))
				return true;  // keep color as is

            wxColour newColor(wxString::FromAscii(theColor));
            bool result = newColor.Ok();
            labelcolor = result ? newColor : labelcolor;
            return result;
        }
        const wxColour& GetLabelColor() const {return labelcolor;}
        
        // symbol stuff
        enum SdtNodeSymbol { ELLIPSE,
                            CIRCLE,
                            RECTANGLE,
                            SQUARE,
                            RND_RECTANGLE,
                            RND_SQUARE,
                            NO_SYMBOL };

        SdtNodeSymbol GetSymbol() const {return symbol;}
        bool SetSymbol(const char* val);
        bool SetSymbolColor(const char* theColor)
        {
			if (!theColor || !strcmp(theColor, "X"))
				return true;  // leave as is

            wxColour newColor(wxString::FromAscii(theColor));
            bool result = newColor.Ok();
            symbolcolor = result ? newColor : symbolcolor;
            return result;
        }
        bool SetSymbolThickness(const char* thickness) 
        {
			if (!thickness || !strcmp(thickness, "X"))
				return true;  // leave as is

			int theThickness = atoi(thickness);
            bool result = (theThickness > 0);
            result = result && (theThickness <= 8);
            symbolthickness = result ? theThickness : symbolthickness;
            return result;
        }
        bool SetCircleXRadius(const char* radius)
        {
			if (!radius || !strcmp(radius, "X"))
				return true;  // leave as is

			double theRadius = atof(radius);
            bool result = (theRadius > 0);
            circlexradius = result? theRadius : circlexradius;
            return result;
        }
        bool SetCircleYRadius(const char* radius)
        {
			if (!radius || !strcmp(radius, "X"))
				return true;  // leave as is

			double theRadius = atof(radius);
            bool result = (theRadius > 0);
            circleyradius = result? theRadius : circleyradius;
            return result;
        }
        const wxColour& GetSymbolColor() const {return symbolcolor;}
        int GetSymbolThickness() const {return symbolthickness;}
        double GetCircleXRadius() const {return circlexradius;}
        double GetCircleYRadius() const {return circleyradius;}
        
        SdtNode* Next() {return next;}
        const SdtSprite* Sprite() {return sprite;}

    private:
        const char*      name;
        char*            labeltext;
        double           pos_x;
        double           pos_y;
        const SdtSprite* sprite;
        bool             label;
        wxColour         labelcolor;
        SdtNodeSymbol    symbol;
        wxColour         symbolcolor;
        int              symbolthickness;
        double           circlexradius;
        double           circleyradius;
        
        unsigned int     index;
        
        SdtNode*         prev;
        SdtNode*         next;
};  // end class SdtNode

class SdtNodeList
{
    public:
        SdtNodeList();
        ~SdtNodeList();
        void Destroy();
        
        void Prepend(SdtNode* node);
        void Append(SdtNode* node);
        void Remove(SdtNode* node);
		void RemoveSprites();
		void RemoveSymbols();
        SdtNode* Head() {return head;}
        SdtNode* FindNodeByName(const char* nodeName);
        SdtNode* FindNodeByPosition(long x_pos, long y_pos);
    private:
        SdtNode* head;
        SdtNode* tail;
};  // end class SdtNodeList

class SdtLink
{
    friend class SdtLinkList;
    
    public:
        SdtLink(const SdtNode* srcNode, const SdtNode* dstNode,
		        const char* linkID, bool directed);
        ~SdtLink();
        
        bool SetColor(const char* lineColor)
        {
			if (!lineColor || !strcmp(lineColor, "X"))
				return true;  // leave color unchanged

            wxColour newColor(wxString::FromAscii(lineColor));
            bool result = newColor.Ok();
            color = result ? newColor : color;
            return result;
        }
        bool SetThickness(const char* lineThickness) 
        {
			if (!lineThickness || !strcmp(lineThickness, "X"))
				return true;  // leave thickness unchanged

			int theThickness = atoi(lineThickness);
            bool result = (theThickness > 0);
            result = result && (theThickness <= 8);
            thickness = result ? theThickness : thickness;
            return result;
        }
        
        const wxColour& GetColor() const {return color;}
        int GetThickness() const {return thickness;}
        
		bool Match(const SdtNode* theSrc, const SdtNode* theDst,
		           const char* linkID, bool directedLink) const;

		enum LabelState
		{
			LABEL_CLEAR,
			LABEL_OFF,
			LABEL_ON
		};

        const SdtNode* Src() {return src;}
        const SdtNode* Dst() {return dst;}
        SdtLink* Next() {return next;}
		SdtLink* GetNextMlink() { return next_mult; }
        const char* GetLinkID() { return link_ID; }
		void  SetLinkLabelOff() { link_label_on = LABEL_OFF; }
		void  SetLinkLabelOn() { link_label_on = LABEL_ON; }
		LabelState  LinkLabelState() { return link_label_on; }
		bool  SetLinkLabelColor(const char* labelColor);
		const wxColour& GetLinkLabelColor() { return link_label_color; }
		bool  SetLinkLabelText(const char* labelText);
		const char* GetLinkLabelText() { return (link_label_text[0] != '\0' ? link_label_text : NULL); }
		void  SetLinkLabelTextDefault();
		bool  SetLinkLabel(const char* offOnColor, const char* labelText);
		bool  IsHeadMultOrSingle() { return (prev_mult == NULL); }
		bool  IsHeadMult() { return ((prev_mult == NULL) && (next_mult != NULL)); }
		bool  IsDirected() {return directed; }
		int CountMults(SdtLink* headMult);

		long GetCenterX() { return static_cast<long>(x_center + 0.5); }
		long GetCenterY() { return static_cast<long>(y_center + 0.5); }
		long GetIncX(SdtLink* headMult);
		long GetIncY(SdtLink* headMult);

		long GetRemappedSrcX() { return src_x_remapped; }
		long GetRemappedSrcY() { return src_y_remapped; }
		long GetRemappedDstX() { return dst_x_remapped; }
		long GetRemappedDstY() { return dst_y_remapped; }

		void CalcArcParams();
		void RemapNodeSwap();

    private:
        const SdtNode* src;
        const SdtNode* dst;
        wxColour      color;
        int           thickness;
 
        SdtLink* prev;
        SdtLink* next;
        char     link_ID[CMD_LINE_MAX];
		LabelState  link_label_on;
		wxColour link_label_color;
		char     link_label_text[CMD_LINE_MAX];

		SdtLink* prev_mult;  // linked list of links between same nodes
		SdtLink* next_mult;

		double x_center;
		double y_center;
		double x_inc;
		double y_inc;

		long src_x_remapped;
		long src_y_remapped;
		long dst_x_remapped;
		long dst_y_remapped;
		bool	directed;
		bool link_label_color_set;
};  // end class SdtLink

class SdtLinkList
{
    public:
        SdtLinkList();
        ~SdtLinkList();
        void Destroy();
        SdtLink* Head() {return head;}
        SdtLink* AddLink(const SdtNode* src, const SdtNode* dst,
		                 const char* linkID, const char* directedLink);
        bool RemoveLink(const SdtNode* src, const SdtNode* dst,
		                const char* linkID, const char* dir);
        SdtLink* FindLink(const SdtNode* src, const SdtNode* dst,
		                  const char* linkID, const char* dir);
        void RemoveLinks(const SdtNode* node);    
		void RemoveLinkLabels();
        void Remove(SdtLink* link);
 
    private:
        void Append(SdtLink* link);
		SdtLink* HeadMultipleLink(SdtLink* link);
		void  AddToMultList(SdtLink* multHead, SdtLink* link);
            
        SdtLink* head;
        SdtLink* tail;
}; // end class SdtLinkList;

class SdtMapListener
{
    public:
        SdtMapListener();
		virtual ~SdtMapListener();
        virtual void OnMapDoubleClick(long x, long y) = 0;
		virtual void OnMapShiftClick(long x, long y, int max_x, int max_y) = 0;
        virtual void OnMapResize(long width, long height) = 0;
};  // end class SdtMapListener;

class SdtMapCanvas : public wxScrolledWindow
{
    public:
        SdtMapCanvas(SdtMapListener* theListener = NULL);
        ~SdtMapCanvas();
        bool Create(wxWindow* parent);
        
        bool SetSize(long width, long height);
        wxBitmap* GetBitmap() {return render_bitmap;}       
        long GetMapWidth()
            {return (render_bitmap ? render_bitmap->GetWidth() : 0);}
        long GetMapHeight()
            {return (render_bitmap ? render_bitmap->GetHeight() : 0);}
        void CenterOn(int x, int y);
        void ScreenCap();
        
    private:
        long            last_y;
        SdtMapListener*  listener;
        wxBitmap*       render_bitmap;
        unsigned int    printseqno;
        
        void OnMouse(wxMouseEvent& event);
        void OnPaint(wxPaintEvent& event);
        
        DECLARE_DYNAMIC_CLASS(SdtMapCanvas)
        DECLARE_EVENT_TABLE()
};  // end class SdtMapCanvas

        
class SdtFrameListener
{
    public:
        SdtFrameListener();
		virtual ~SdtFrameListener();
        virtual void OnFrameDestruction(class SdtFrame* theFrame) = 0;
};  // end class SdtFrameListener;

class SdtFrame : public wxFrame
{
    friend class SdtFrameList;
    
    public:
        SdtFrame(SdtFrameListener* theListener = NULL);
        ~SdtFrame();
        void OnClose(wxCloseEvent& event);
        bool Create(const char* title);
        
    private:        
        SdtFrameListener*    listener;    
        SdtFrame*            next;
        
        DECLARE_DYNAMIC_CLASS(SdtFrame)
        DECLARE_EVENT_TABLE()
};  // end class SdtFrame

    
class SdtFrameList
{
    public:
        SdtFrameList();
        ~SdtFrameList();
        void Destroy();
        void Prepend(SdtFrame* frame)
        {
			if (!frame)
				return;

            frame->next = head;
            head = frame;   
        }
        SdtFrame* Remove(SdtFrame* frame);
        SdtFrame* FindFrameByTitle(const char* title);
        
    private:
        SdtFrame* head;
};  // end class SdtFrameList

class SdtPopup : public SdtFrame
{
    public:
        SdtPopup(SdtFrameListener* theListener = NULL);
        ~SdtPopup();
        bool Create(const char* title);
        bool SetContent(const char* content);
        void Resize();
        // hack
        void ResizeErrorPopup();

    private:
        wxStaticText text;
        bool         show;
        
        DECLARE_DYNAMIC_CLASS(SdtPopup)
};  // end class SdtPopup

class SdtInputStack
{
    public:
        class Item
        {
            friend class SdtInputStack;
            public:
            private:
                Item(ProtoDispatcher::Descriptor theFile);
                ProtoDispatcher::Descriptor file;
                Item*  next;
        };

        SdtInputStack();
        ~SdtInputStack();
        bool Push(ProtoDispatcher::Descriptor theFile);
        bool AddLast(ProtoDispatcher::Descriptor theFile);
        ProtoDispatcher::Descriptor Pop();
        
    private:
        
        Item*   head;
};  // end class SdtInputStack
        
class SdtApp : public wxProtoApp, public SdtFrameListener, public SdtMapListener
{
     public:
        SdtApp();
        ~SdtApp();

		enum DirType {NO_ALLS, BI_ONLY, UNI_ONLY, BI_AND_BOTH_UNI, SAME_LINKID};

        bool OnCommand(const char* cmd, const char* val);
        bool OnPipeCommand(char* input_buffer);
        //int MainLoop();
        
        void OnFrameDestruction(SdtFrame* theFrame);
        void OnMapDoubleClick(long x, long y);
		void OnMapShiftClick(long x, long y, int max_x, int max_y);
        void OnMapResize(long width, long height)
        {
            SetBackgroundSize(width, height);    
        }
        
	    bool SetInput(const char* fileName);
        bool SetPath(const char* pathName);
		bool SetPipe(const char* pipeName);
        bool FindFile(char* imageFile);
        bool SetBackgroundImage(const char* imageFile);
        bool SetBackgroundSize(long width, long height);
        bool ScaleBackground(double factor);
        void SetBounds(double left, double top, double right, double bottom);
        bool InBounds(double x, double y);
        long RemapX(double x);
        long RemapY(double y);
        // "distance" remap functions, i.e. not offset by left and top
        long RemapXDist(double x);
        long RemapYDist(double y);
        
        bool SetSprite(const char* name);
        bool SetSpriteImage(const char* fileName)
        {
            // find the imageFile as named or in the specified path
            char imageFile[8192];
            strcpy(imageFile, fileName);  // allows path prepending
            if (!SdtApp::FindFile(imageFile)) return false;
            
            
            return (current_sprite ? current_sprite->SetImage(imageFile) : false);
        }
        bool SetSpriteSize(long width, long height)
            {return (current_sprite ? current_sprite->SetSize(width, height) : false);}
        bool ScaleSprite(double factor)
            {return (current_sprite ? current_sprite->Scale(factor) : false);}

        bool SetStatus(const char* status);
        bool SetNode(const char* name);
        bool SetNodeType(const char* spriteName);
		bool SetNodeNumPosition(double x, double y);
        bool SetNodePosition(const char* x, const char* y);
		bool SetNodeLabel(const char* offOnColorText);
        bool DeleteNode(const char* name);
	        
        bool AddLink(const char* src, const char* dst,
		             const char* linkID, const char* directed);
        bool RemoveLink(const char* src, const char* dst,
		                const char* linkID, const char* directed);
        
        void Update(bool advance);
        virtual bool OnInit();
        
        bool GetInput();
        void OnAnimationTimeout(wxTimerEvent& event);
        void OnIdle(wxIdleEvent& event);
        void OnQuit(wxCommandEvent& event);
		void OnFileOpen(wxCommandEvent& event);
        void OnAbout(wxCommandEvent& event);      
		void OnButtonOK(wxCommandEvent& event);
        void OnFill(wxCommandEvent& event)
            {FillWindow();}
        void OnScreenCap(wxCommandEvent& event)
            {if (map_canvas) map_canvas->ScreenCap();}
        void OnAuto(wxCommandEvent& event)
            {AutoSize();}
	    void AutoSize();
	    void FillWindow();
		void OnLabels(wxCommandEvent& event);
		void OnMlinks(wxCommandEvent& event);

        bool SetPopup(const char* title);
        bool SetPopupContent(const char* content);
        bool ResizePopup();
        void DeletePopup(const char* title);
	bool OnUpdate(ProtoTimer& theTimer)
	{
        Update(true); 
        return true;
    }
	static void GetInputCallBack(ProtoDispatcher::Descriptor descriptor, ProtoDispatcher::Event theEvent, const void* userData);
	void GetNextFile();
	bool OnWaitTimeout(ProtoTimer& theTimer);
	bool SetLinkLabel(const char* offOnColorText);
	bool OldSyntax(const char* colorLinkID, const char* widthDirected);
	bool MatchingLink(SdtNode* src, SdtNode* dst,
	                  DirType linkDirectedness, SdtLink* currentLink);
	bool HasDblCommaLinkID(char* dcCopy);

	void CloseScript(ProtoDispatcher::Descriptor fileID)
	{
#ifdef WIN32
		CloseHandle(fileID);
#else
		close(fileID);
#endif
	}

	ProtoDispatcher::Descriptor OpenScript(const char* fileName)
	{
		ProtoDispatcher::Descriptor scriptFileID;

#ifdef WIN32

#ifdef _UNICODE  // CreateFile will become CreateFileW
		wxString wideFileName = wxString::FromAscii(fileName);
		const wxChar* cfFile = wideFileName.fn_str();
#else  // !_UNICODE - CreateFile will become CreateFileA
		const char* cfFile = fileName;
#endif  // _UNICODE

		scriptFileID = CreateFile(cfFile,
		                          GENERIC_READ,
		                          FILE_SHARE_READ,
		                          NULL,
		                          OPEN_EXISTING,
		                          FILE_ATTRIBUTE_NORMAL,
		                          NULL);
#else  // !WIN32
		scriptFileID = open(fileName, O_RDONLY);
#endif  // WIN32

		return scriptFileID;
	}

    private:
        enum 
        {
            ID_QUIT = wxID_HIGHEST+1,
			ID_FILEOPEN,
            ID_ABOUT,
	        ID_AUTO,
	        ID_FILL,
            ID_SCRCAP,
			ID_LABELS,
			ID_MLINKS
        }; 
       // enum {ANIMATION_TIMER = 1};
        enum CmdType {CMD_INVALID, CMD_ARG, CMD_NOARG};

        static CmdType GetCmdType(const char* string);
        static void Usage();
        
        void OnControlMsg(ProtoSocket&       /*thePipe*/,  
                          ProtoSocket::Event theEvent);
        bool SetLine(const char* colorLinkID, const char* widthDirected);

	    static const char* const CMD_LIST[];

        SdtFrame*                   map_frame;      
        SdtMapCanvas*               map_canvas;     
        SdtSpriteList               sprite_list;    
        SdtSprite*                  current_sprite; 
        SdtSprite*                  pipe_current_sprite;
        SdtNodeList                 node_list;      
        SdtNode*                    current_node;
        SdtNode*                    pipe_current_node;
        SdtFrameList                popup_list;     
        SdtPopup*                   current_popup;  
        SdtLinkList                 link_list;      
        wxImage                     bg_image;
        wxBitmap*                   bg_bitmap;
        long                        bg_imagewidth;
        long                        bg_imageheight;
        double                      bg_top;
        double                      bg_bottom;
        double                      bg_left;
        double                      bg_right;
        bool                        seeking_cmd;
        bool                        pipe_seeking_cmd;
        char                        current_cmd[PIPE_MAX];
        char                        pipe_current_cmd[PIPE_MAX];
        char                        pipe_pending_cmd[PIPE_MAX];
        char                        default_path[PATH_NAME_MAX];
        char                        pipe_name[PIPE_NAME_MAX];
        char                        script_path[FILE_NAME_MAX];

        ProtoDispatcher::Descriptor	current_input;
        //unsigned long               input_wait;
        double                      input_wait;
        SdtInputStack               input_stack;
        char                        input_buffer[PIPE_MAX];
        unsigned int                input_index;
        bool                        quoting;

        //wxTimer                     animation_timer;
        ProtoTimer			        update_timer;
        bool                        running;
        ProtoTimer			        wait_timer;
        ProtoPipe                   control_pipe;
        char                        status_node[STATUS_MAX];

        char title[CMD_LINE_MAX];        // used for labeling the main SDT window titlebar
        SdtLink*                    current_link;  // allows link & line to work together
		bool 						comment_in_effect;
		bool						toggle_on;  // turn link labels on or off
		bool						mlinks_on;
		bool						link_in_effect;

		SdtNode*					current_src;  // for "all,*" options
		SdtNode*					current_dst;
		DirType						current_directedness;
		char						current_link_ID[CMD_LINE_MAX];
#ifdef WIN32
        bool                        console_input;
#endif // WIN32

        DECLARE_EVENT_TABLE()
};  // end class SdtApp


#endif // _SDT
