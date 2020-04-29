package mil.navy.nrl.sdt3d;

abstract class SdtModel extends SdtSprite implements SdtModelDimensions
{
	public SdtModel(String name)
	{
		super(name);
	}
	
	public SdtModel(SdtSprite template) 
	{
		super(template);
	}

	// Default to useAbsoluteYaw to false so any node heading will be used
	// if no orientation is set
	private boolean useAbsoluteYaw = false;


	@Override
	public void setAbsoluteYaw(boolean useAbsolute)
	{
		this.useAbsoluteYaw = useAbsolute;
	}


	public boolean useAbsoluteYaw()
	{
		return this.useAbsoluteYaw;
	}

	boolean isValid() {
		// TODO Auto-generated method stub
		return false;
	}

	
}
