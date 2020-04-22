package mil.navy.nrl.sdt3d;

interface SdtModelDimensions extends SdtSpriteDimensions
{
	void setUseLighting(boolean useLighting);
	
	void setModelPitch(double degrees);
	
	void setModelYaw(double degrees);

	void setModelRoll(double degrees);
	
	double getModelPitch();
	
	double getModelYaw();
	
	double getModelRoll();
	

}
