/**
 * This program opens an SDT script file and "plays" it with
 * appropriate timing (following) wait commands to 
 * the "sdt3d" ProtoPipe
 */
 
#include "protokit.h"
#include <stdio.h>  // for stderr output as needed
#include <stdlib.h> // for atoi()
#include <ctype.h>  // for isspace(), etc

class FastReader
{
    public:
        enum Result {OK, ERROR_, DONE, TIMEOUT};
        FastReader();
        FastReader::Result Read(FILE* filePtr, char* buffer, unsigned int* len, 
                                double timeout = -1.0);
        FastReader::Result Readline(FILE* filePtr, char* buffer, unsigned int* len,
                                    double timeout = -1.0);

    private:
        enum {BUFSIZE = 2048};
        char         savebuf[BUFSIZE];
        char*        saveptr;
        unsigned int savecount;
};  // end class FastReader

class SdtPlayer : public ProtoApp
{
    public:
        SdtPlayer();
        ~SdtPlayer();

        // Overrides from ProtoApp or NsProtoSimAgent base
        bool OnStartup(int argc, const char*const* argv);
        bool ProcessCommands(int argc, const char*const* argv);
        void OnShutdown();
        
        bool OnCommand(const char* cmd, const char* val);
            
    private:
        enum CmdType {CMD_INVALID, CMD_ARG, CMD_NOARG};
        static CmdType GetCmdType(const char* string);
        static const char* const CMD_LIST[];        
        void Usage();
        
        bool OnPlaybackTimeout(ProtoTimer& theTimer);
        
        ProtoTimer   playback_timer;
        ProtoPipe    sdt_pipe;
        FILE*        file_ptr;
        FastReader   file_reader;
        char         line_buffer[8192];
        unsigned int line_length; 
            
            
};  // end class SdtPlayer

// Instantiate our application instance 
PROTO_INSTANTIATE_APP(SdtPlayer) 
        
SdtPlayer::SdtPlayer()
 : sdt_pipe(ProtoPipe::MESSAGE), line_length(0)
{
    playback_timer.SetListener(this, &SdtPlayer::OnPlaybackTimeout);
}

SdtPlayer::~SdtPlayer()
{
    
}

const char* const SdtPlayer::CMD_LIST[] =
{
    "+input",     // sdt file to use
    NULL
};
    
void SdtPlayer::Usage()
{
    fprintf(stderr, "sdtPlayer input <fileName>\n");
}  // end SdtPlayer::Usage()
  
bool SdtPlayer::OnStartup(int argc, const char*const* argv)
{
    if (argc < 2)
	{
		Usage();
		return false;
	}
	if (!ProcessCommands(argc, argv))
    {
        PLOG(PL_ERROR, "SdtPlayer::OnStartup() error processing command line\n");
		Usage();
        return false;
    }
    
    if (!sdt_pipe.Connect("sdt3d"))
    {
        PLOG(PL_ERROR, "sdtplay error: couldn't connect to sdt3d instance\n");
        return false;
    }        
    playback_timer.SetRepeat(-1);
    playback_timer.SetInterval(0.0);
    ActivateTimer(playback_timer);
    return true;
}  // end SdtPlayer::OnStartup()

void SdtPlayer::OnShutdown()
{
    if (NULL != file_ptr) 
    {
        fclose(file_ptr);
        file_ptr = NULL;
    }
    if (playback_timer.IsActive()) playback_timer.Deactivate();
    if (sdt_pipe.IsOpen()) sdt_pipe.Close();
    
}  // end SdtPlayer::OnShutdown()

bool SdtPlayer::OnCommand(const char* cmd, const char* val)
{
    CmdType type = GetCmdType(cmd);
    ASSERT(CMD_INVALID != type);
    unsigned int len = strlen(cmd);
    if ((CMD_ARG == type) && !val)
    {
        PLOG(PL_ERROR, "SdtPlayer::ProcessCommand(%s) missing argument\n", cmd);
        return false;
    }
    else if (!strncmp("input", cmd, len))
    {
        file_ptr = fopen(val, "rb");
        if (NULL == file_ptr)
        {
            PLOG(PL_ERROR, "SdtPlayer::ProcessCommand(%s) missing argument\n", cmd);
            return false;
        }
    }
    else
    {
        PLOG(PL_ERROR, "SdtPlayer::OnCommand() unknown command error?\n");
        return false;
    }
    return true;
}  // end SdtPlayer::OnCommand()
    
bool SdtPlayer::OnPlaybackTimeout(ProtoTimer& /*theTimer*/)
{
    while (1)
    {
        if (0 == line_length)
        {
            line_length = 8192;
            FastReader::Result result = 
                file_reader.Readline(file_ptr, line_buffer, &line_length);
            switch (result)
            {
                case FastReader::OK:
                case FastReader::DONE:
                    break;

                default:
                    // error or end of file
                    playback_timer.Deactivate();
                    dispatcher.Stop();
                    return false;
            }

            unsigned long msec;
            if (2 == sscanf(line_buffer, "wait %lu", &msec))
            {
                double interval = ((double)msec) / 1000.0;
                playback_timer.SetInterval(interval);
                line_length = 0;
                return true;
            }
        }
        
        if (!sdt_pipe.Send(line_buffer, line_length))
        {
            playback_timer.SetInterval(0.10);
            return true;
        }
    }
    return true;
}  // end SdtPlayer::OnPlaybackTimeout()



SdtPlayer::CmdType SdtPlayer::GetCmdType(const char* cmd)
{
    if (!cmd) return CMD_INVALID;
    unsigned int len = strlen(cmd);
    bool matched = false;
    CmdType type = CMD_INVALID;
    const char* const* nextCmd = CMD_LIST;
    while (*nextCmd)
    {
        if (!strncmp(cmd, *nextCmd+1, len))
        {
            if (matched)
            {
                // ambiguous command (command should match only once)
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
}  // end SdtPlayer::GetCmdType()

bool SdtPlayer::ProcessCommands(int argc, const char*const* argv)
{
    // Dispatch command-line commands to our OnCommand() method
    int i = 1;
    while ( i < argc)
    {
        // Is it a class sdtPlayer command?
        switch (GetCmdType(argv[i]))
        {
            case CMD_INVALID:
            {
                PLOG(PL_ERROR, "SdtPlayer::ProcessCommands() Invalid command:%s\n", 
                        argv[i]);
                return false;
            }
            case CMD_NOARG:
                if (!OnCommand(argv[i], NULL))
                {
                    PLOG(PL_ERROR, "SdtPlayer::ProcessCommands() ProcessCommand(%s) error\n", 
                            argv[i]);
					return false;
                }
                i++;
                break;
            case CMD_ARG:
                if (!OnCommand(argv[i], argv[i+1]))
                {
                    PLOG(PL_ERROR, "SdtPlayer::ProcessCommands() ProcessCommand(%s, %s) error\n", 
                            argv[i], argv[i+1]);
                    return false;
                }
                i += 2;
                break;
        }
    }
    return true;  
}  // end SdtPlayer::ProcessCommands()

FastReader::FastReader()
    : savecount(0)
{
    
}

FastReader::Result FastReader::Read(FILE*           filePtr, 
                                    char*           buffer, 
                                    unsigned int*   len,
                                    double          timeout)
{
    unsigned int want = *len;   
    if (savecount)
    {
        unsigned int ncopy = MIN(want, savecount);
        memcpy(buffer, saveptr, ncopy);
        savecount -= ncopy;
        saveptr += ncopy;
        buffer += ncopy;
        want -= ncopy;
    }
    while (want)
    {
        unsigned int result;
#ifndef WIN32 // no real-time TRPR for WIN32 yet
        if (timeout >= 0.0)
        {
            int fd = fileno(filePtr);
            fd_set input;
            FD_ZERO(&input);
            struct timeval t;
            t.tv_sec = (unsigned long)timeout;
            t.tv_usec = (unsigned long)((1.0e+06 * (timeout - (double)t.tv_sec)) + 0.5);
            FD_SET(fd, &input);
            int status = select(fd+1, &input, NULL, NULL, &t);
            switch(status)
            {
                case -1:
                    if (EINTR != errno) 
                    {
                        perror("trpr: FastReader::Read() select() error");
                        return ERROR_; 
                    }
                    else
                    {
                        continue;   
                    }
                    break;
                    
                case 0:
                    return TIMEOUT;
                    
                default:
                    result = fread(savebuf, sizeof(char), 1, filePtr);
                    break;
            } 
        }
        else
#endif // !WIN32
        {
            // Perform buffered read when there is no "timeout"
            result = fread(savebuf, sizeof(char), BUFSIZE, filePtr);
            // This check skips NULLs that have been read on some
            // use of trpr via tail from an NFS mounted file
            if (!isprint(*savebuf) && 
                ('\t' != *savebuf) &&
                ('\n' != *savebuf) && 
                ('\r' != *savebuf))
                    continue;
        }
        if (result)
        {
            unsigned int ncopy= MIN(want, result);
            memcpy(buffer, savebuf, ncopy);
            savecount = result - ncopy;
            saveptr = savebuf + ncopy;
            buffer += ncopy;
            want -= ncopy;
        }
        else  // end-of-file
        {
#ifndef WIN32
            if (ferror(filePtr))
            {
                if (EINTR == errno) continue;   
            }
#endif // !WIN32
            *len -= want;
            if (*len)
                return OK;  // we read at least something
            else
                return DONE; // we read nothing
        }
    }  // end while(want)
    return OK;
}  // end FastReader::Read()

// An OK text readline() routine (reads what will fit into buffer incl. NULL termination)
// if *len is unchanged on return, it means the line is bigger than the buffer and 
// requires multiple reads

FastReader::Result FastReader::Readline(FILE*         filePtr, 
                                        char*         buffer, 
                                        unsigned int* len, 
                                        double        timeout)
{   
    unsigned int count = 0;
    unsigned int length = *len;
    char* ptr = buffer;
    while (count < length)
    {
        unsigned int one = 1;
        switch (Read(filePtr, ptr, &one, timeout))
        {
            case OK:
                if (('\n' == *ptr) || ('\r' == *ptr))
                {
                    *ptr = '\0';
                    *len = count;
                    return OK;
                }
                count++;
                ptr++;
                break;
                
            case TIMEOUT:
                // On timeout, save any partial line collected
                if (count)
                {
                    savecount = MIN(count, BUFSIZE);
                    if (count < BUFSIZE)
                    {
                        memcpy(savebuf, buffer, count);
                        savecount = count;
                        saveptr = savebuf;
                        *len = 0;
                    }
                    else
                    {
                        memcpy(savebuf, buffer+count-BUFSIZE, BUFSIZE);
                        savecount = BUFSIZE;
                        saveptr = savebuf;
                        *len = count - BUFSIZE;
                    }
                }
                return TIMEOUT;
                
            case ERROR_:
                return ERROR_;
                
            case DONE:
                return DONE;
        }
    }
    // We've filled up the buffer provided with no end-of-line 
    return ERROR_;
}  // end FastReader::Readline()

