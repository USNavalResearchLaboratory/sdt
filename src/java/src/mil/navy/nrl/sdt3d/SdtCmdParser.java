package mil.navy.nrl.sdt3d;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import mil.navy.nrl.sdt3d.sdt3d.AppFrame.Time;

public class SdtCmdParser 
{

	enum CmdType {
		CMD_INVALID, CMD_ARG, CMD_NOARG
	};

	static String CMD_LIST[] = {
		"+bgbounds",
		"+collapseLinks",
		"+flyto",
		"+zoom",
		"+heading",
		"+pitch",
		"+instance",
		"+sprite",
		"+image",
		"+size",
		"+length",
		"+scale",
		"+node",
		"+type",
		"+position",
		"+pos",
		"+focus",
		"+follow",
		"+center",
		"+path",
		"+label",
		"+symbol",
		"+shape",
		"+region",
		"+delete", // remove node and any links to it
		"+clear", // remove all nodes and links
		"+link",
		"+light",
		"+linklabel",
		"+loadCache",
		"+logDebugOutput",
		"+logfile",
		"+unlink",
		"+line",
		"+wait",
		"+input",
		"+tile",
		"+tileimage",
		"+trail",
		"+sector",
		"+status",
		"+defaultAltitudeType",
		"+listen",
		"-off",
		"+popup",
		"-popdown",
		"+view",
		"+viewSource",
		"+viewXml",
		"+layer",
		"+nodeLayer",
		"+linkLayer",
		"+symbolLayer",
		"+labelLayer",
		"+regionLayer",
		"+tileLayer",
		"+title",
		"+elevationOverlay",
		"+file",
		"+geoTiff",
		"+geoTiffFile",
		"+backgroundColor",
		"+flatEarth",
		"+elevationData",
		"+kml",
		"+kmlFile",
		"+stereo",
		"+offlineMode",
		"+origin",
		"-reset",
		"-resetPerspective",
		"+lookat",
		"+userConfigFile",
		"+symbolOffset",
		"+orientation",
		"+offset",
		"+enableSdtViewControls",
		"+showLayerPanel",
		"+showSdtPanel",
		"+showStatusPanel",
		"+showGoToPanel",
		"+showWmsPanel",
		"+showSdtControlPanel",
		"+multiFrame",
		"+modelJarFile",
		null
	};
	
	String pending_cmd = null;

	boolean seeking_cmd = true;

	String current_cmd = null;

	SdtSprite currentSprite = null;

	SdtNode currentNode = null;

	SdtRegion currentRegion = null;

	SdtSymbol currentSymbol = null;

	List<SdtLink> currentLinkSet = new ArrayList<SdtLink>();

	SdtTile currentTile = null;

	String currentView = null;

	String currentGeoTiff = null;

	SdtSpriteKml currentKml = null;

	boolean pipeCmd = false;

	StringWriter buffer = new StringWriter();

	PrintWriter out = new PrintWriter(buffer);

	private sdt3d.AppFrame sdt3dApp = null;
	

	public SdtCmdParser(sdt3d.AppFrame theApp)
	{
		this.sdt3dApp = theApp;
	}

	/**
	 * Called by the thread sychronized sdt3d::OnInput loop, this
	 * method is responsible for waiting for command arguments
	 * if necessary and invoking sdt3d.processCommand()
	 * 
	 * @param str
	 * @return
	 */
	public boolean OnCommand(String str, boolean scenarioCmd)
	{
		// System.out.println("OnCommand(" + str + ")");

		str = str.trim();

		if (null == pending_cmd)
			pending_cmd = str;

		if (seeking_cmd)
		{
			switch (GetCmdType(pending_cmd))
			{
				case CMD_ARG:
					current_cmd = pending_cmd;
					seeking_cmd = false;
					break;
				case CMD_NOARG:
					sdt3dApp.processCmd(pending_cmd, null, scenarioCmd); // ljt error checking?
					pending_cmd = null;
					seeking_cmd = true;
					break;
				default:
					seeking_cmd = true;
					pending_cmd = null;
					return false;
			} // end switch
		}
		else // Not seeking command
		{
			sdt3dApp.processCmd(current_cmd, str, scenarioCmd);
			seeking_cmd = true;
			pending_cmd = null;
		} // done seeking cmd

		return true;
	} // end OnCommand

	
	public CmdType GetCmdType(String cmd)
	{

		if (0 == cmd.length())
			return CmdType.CMD_INVALID;
		boolean matched = false;
		CmdType type = CmdType.CMD_INVALID;
		String[] nextCmd = CMD_LIST;
		int i = 0;
		while (i < CMD_LIST.length - 1)
		{
			String validCmd = nextCmd[i].substring(1, nextCmd[i].length());
			if (validCmd.equalsIgnoreCase(cmd))
			{
				if (matched)
				{
					// ambiguous command (cmd should match only once)
					return CmdType.CMD_INVALID;
				}
				else
				{
					matched = true;
					if (nextCmd[i].startsWith("+"))
						type = CmdType.CMD_ARG;
					else
						type = CmdType.CMD_NOARG;
				}
			}
			i++;
		} // end while
		return type;
	} // end GetCmdType

	
	void setPipeCmd(boolean isPipeCmd)
	{
		pipeCmd = isPipeCmd;
	}


	boolean doCmd(String pendingCmd, String val)
	{
		if (pendingCmd.equalsIgnoreCase("bgbounds"))
			return sdt3dApp.setBackgroundBounds(val);
		else if (pendingCmd.equalsIgnoreCase("flyto"))
			return sdt3dApp.setFlyTo(val);
		else if (pendingCmd.equalsIgnoreCase("zoom"))
			return sdt3dApp.setZoom(val);
		else if (pendingCmd.equalsIgnoreCase("heading"))
			return sdt3dApp.setHeading(val);
		else if (pendingCmd.equalsIgnoreCase("pitch"))
			return sdt3dApp.setPitch(val);
		else if (pendingCmd.equalsIgnoreCase("tileImage"))
			return sdt3dApp.setTileImage(val);
		else if (pendingCmd.equalsIgnoreCase("tile"))
			return sdt3dApp.setTile(val);
		else if (pendingCmd.equalsIgnoreCase("sector"))
			return sdt3dApp.loadTile(val);
		else if (pendingCmd.equalsIgnoreCase("instance"))
			return sdt3dApp.setPipeName(val);
		else if (pendingCmd.equalsIgnoreCase("bgimage"))
			return false;
		else if (pendingCmd.equalsIgnoreCase("sprite"))
				return sdt3dApp.setSprite(val);
		else if (pendingCmd.equalsIgnoreCase("scale"))
			return sdt3dApp.setScale(val);
		else if (pendingCmd.equalsIgnoreCase("image"))
		{
			// sprite file not found
			if (!sdt3dApp.setImage(val))
			{
				// Invalid image assigned, reset our state for the sprite
				// so we can reassign it to the same name if need be.
				sdt3dApp.spriteTable.remove(currentSprite.getName());
				currentSprite = null;
				return false;
			}
			return true;
		}
		else if (pendingCmd.equalsIgnoreCase("node"))
			return sdt3dApp.setNode(val);
		else if (pendingCmd.equalsIgnoreCase("type"))
			return sdt3dApp.setType(val);
		else if (pendingCmd.equalsIgnoreCase("position") || pendingCmd.equalsIgnoreCase("pos"))
			return sdt3dApp.setPosition(val);
		else if (pendingCmd.equalsIgnoreCase("focus"))
			return sdt3dApp.setFocus(val);
		else if (pendingCmd.equalsIgnoreCase("follow"))
			return sdt3dApp.setFollow(val);
		else if (pendingCmd.equalsIgnoreCase("center"))
			return sdt3dApp.setRegionPosition(val);
		else if (pendingCmd.equalsIgnoreCase("clear"))
			return sdt3dApp.clear(val);
		else if (pendingCmd.equalsIgnoreCase("delete"))
			return sdt3dApp.delete(val);
		else if (pendingCmd.equalsIgnoreCase("size"))
			return sdt3dApp.setSize(val);
		else if (pendingCmd.equalsIgnoreCase("length"))
			return sdt3dApp.setLength(val);
		else if (pendingCmd.equalsIgnoreCase("light"))
			return sdt3dApp.setLight(val);
		else if (pendingCmd.equalsIgnoreCase("label"))
			return sdt3dApp.setLabel(val);
		else if (pendingCmd.equalsIgnoreCase("trail"))
			return sdt3dApp.setTrail(val);
		else if (pendingCmd.equalsIgnoreCase("symbol"))
			return sdt3dApp.setSymbol(val);
		else if (pendingCmd.equalsIgnoreCase("shape"))
			return sdt3dApp.setShape(val);
		else if (pendingCmd.equalsIgnoreCase("link"))
		{
			// Spurious link commands should not be generated as
			// performance may be impacted due to refreshing links...

			return (sdt3dApp.setLink(val));
		}
		else if (pendingCmd.equalsIgnoreCase("linklabel"))
			return sdt3dApp.setLinkLabel(val);
		else if (pendingCmd.equalsIgnoreCase("unlink"))
			return sdt3dApp.setUnlink(val);
		else if (pendingCmd.equalsIgnoreCase("line"))
			return sdt3dApp.setLine(val);
		else if (pendingCmd.equalsIgnoreCase("wait"))
		{
			return true; // wait is implemented in FileThread only
		}
		else if (pendingCmd.equalsIgnoreCase("path"))
			return sdt3dApp.setPath(val);
		else if (pendingCmd.equalsIgnoreCase("modelJarFile"))
			return sdt3dApp.setModelJarFile(val);
		else if (pendingCmd.equalsIgnoreCase("status"))
			return sdt3dApp.setStatus(val);
		else if (pendingCmd.equalsIgnoreCase("region"))
			return sdt3dApp.setRegion(val);
		else if (pendingCmd.equalsIgnoreCase("input"))
			// Files loaded "in line" in scripts should be processed
			// immediately. Note that when an input command is recvd
			// via the input pipe, the file will be appended - pipeCmd
			// flag controls this.
			return sdt3dApp.loadInputFile(val, false);
		else if (pendingCmd.equalsIgnoreCase("title"))
			return sdt3dApp.setAppTitle(val);
		else if (pendingCmd.equalsIgnoreCase("defaultAltitudeType"))
			return sdt3dApp.setDefaultAltitudeType(val);
		else if (pendingCmd.equalsIgnoreCase("listen"))
			return sdt3dApp.setListen(val);
		else if (pendingCmd.equalsIgnoreCase("popup"))
			return sdt3dApp.setPopup(val);
		else if (pendingCmd.equalsIgnoreCase("popdown"))
			return sdt3dApp.setPopdown();
		else if (pendingCmd.equalsIgnoreCase("layer"))
			return sdt3dApp.setLayer(val);
		else if (pendingCmd.equalsIgnoreCase("nodeLayer"))
			return sdt3dApp.setNodeUDLayer(val);
		else if (pendingCmd.equalsIgnoreCase("linkLayer"))
			return sdt3dApp.setLinkUDLayer(val);
		else if (pendingCmd.equalsIgnoreCase("symbolLayer"))
			return sdt3dApp.setSymbolUDLayer(val);
		else if (pendingCmd.equalsIgnoreCase("labelLayer"))
			return sdt3dApp.setLabelUDLayer(val);
		else if (pendingCmd.equalsIgnoreCase("regionLayer"))
			return sdt3dApp.setRegionUDLayer(val);
		else if (pendingCmd.equalsIgnoreCase("tileLayer"))
			return sdt3dApp.setTileUDLayer(val);
		else if (pendingCmd.equalsIgnoreCase("view"))
			return sdt3dApp.setView(val);
		else if (pendingCmd.equalsIgnoreCase("viewXml"))
			return sdt3dApp.setViewXml(val, true);
		else if (pendingCmd.equalsIgnoreCase("backgroundColor"))
			return sdt3dApp.setBackgroundColor(val);
		else if (pendingCmd.equalsIgnoreCase("flatEarth"))
			return sdt3dApp.setFlatEarth(val);
		else if (pendingCmd.equalsIgnoreCase("elevationData"))
			return sdt3dApp.setElevationData(val);
		else if (pendingCmd.equalsIgnoreCase("stereo"))
			return sdt3dApp.setStereo(val);
		else if (pendingCmd.equalsIgnoreCase("offlineMode"))
			return sdt3dApp.setOfflineMode(val);
		else if (pendingCmd.equalsIgnoreCase("collapseLinks"))
			return sdt3dApp.setCollapseLinks(val);
		else if (pendingCmd.equalsIgnoreCase("elevationOverlay"))
			return sdt3dApp.setGeoTiff(val);
		else if (pendingCmd.equalsIgnoreCase("file"))
			return sdt3dApp.setGeoTiffFile(val);
		else if (pendingCmd.equalsIgnoreCase("geoTiff"))
			return sdt3dApp.setGeoTiff(val);
		else if (pendingCmd.equalsIgnoreCase("geoTiffFile"))
			return sdt3dApp.setGeoTiffFile(val);
		else if (pendingCmd.equalsIgnoreCase("kml"))
			return sdt3dApp.setKml(val);
		else if (pendingCmd.equalsIgnoreCase("kmlFile"))
			return sdt3dApp.setKmlFile(val);
		else if (pendingCmd.equalsIgnoreCase("origin"))
			return sdt3dApp.setOrigin(val);
		else if (pendingCmd.equalsIgnoreCase("reset"))
		{
			sdt3dApp.resetSystemState(false);
			return true;
		}
		else if (pendingCmd.equalsIgnoreCase("hardReset"))
		{
			sdt3dApp.resetSystemState(true);
			return true;
		}
		else if (pendingCmd.equalsIgnoreCase("resetPerspective"))
		{
			sdt3dApp.resetPerspective();
			return true;
		}
		else if (pendingCmd.equalsIgnoreCase("lookat"))
		{
			return sdt3dApp.setLookAt(val);

		}
		else if (pendingCmd.equalsIgnoreCase("userConfigFile"))
		{
			return sdt3dApp.loadUserConfigFile(val);
		}
		else if (pendingCmd.equalsIgnoreCase("symbolOffset"))
		{
			return sdt3dApp.setSymbolOffset(val);
		}
		else if (pendingCmd.equalsIgnoreCase("multiFrame"))
		{
			return sdt3dApp.setMultiFrame(val);
		}
		else if (pendingCmd.equalsIgnoreCase("orientation"))
		{
			return sdt3dApp.setOrientation(val);
		}
		else if (pendingCmd.equalsIgnoreCase("offset"))
		{
			return sdt3dApp.setOffset(val);
		}
		else if (pendingCmd.equalsIgnoreCase("enableSdtViewControls"))
		{
			return sdt3dApp.setEnableSdtViewControls(val);
		}
		else if (pendingCmd.equalsIgnoreCase("logDebugOutput"))
		{
			return sdt3dApp.setLogDebugOutput(val);
		}
		else if (pendingCmd.equalsIgnoreCase("loadCache"))
		{
			return sdt3dApp.setLoadCache(val);
		}
		else if (pendingCmd.equalsIgnoreCase("showLayerPanel"))
		{
			return sdt3dApp.setShowLayerPanel(val);
		}
		else if (pendingCmd.equalsIgnoreCase("showSdtPanel"))
		{
			return sdt3dApp.setShowSdtPanel(val);
		}
		else if (pendingCmd.equalsIgnoreCase("showStatusPanel"))
		{
			return sdt3dApp.setShowStatusPanel(val);
		}
		else if (pendingCmd.equalsIgnoreCase("showScenarioPlaybackPanel"))
		{
			return sdt3dApp.setShowScenarioPlaybackPanel(val);
		}
		else if (pendingCmd.equalsIgnoreCase("showWmsFrame"))
		{
			return sdt3dApp.setShowWmsFrame(val);
		}
		else if (pendingCmd.equalsIgnoreCase("showSdtControlPanel"))
		{
			return sdt3dApp.setShowSdtControlPanel(val);
		}
		else
			return false;
	} // end DoCmd


	
}
