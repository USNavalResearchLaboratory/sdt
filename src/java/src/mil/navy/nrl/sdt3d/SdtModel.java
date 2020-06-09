/*********************************************************************
 *
 * AUTHORIZATION TO USE AND DISTRIBUTE
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that: 
 *
 * (1) source code distributions retain this paragraph in its entirety, 
 *  
 * (2) distributions including binary code include this paragraph in
 *     its entirety in the documentation or other materials provided 
 *     with the distribution.
 * 
 *      "This product includes software written and developed 
 *       by Code 5520 of the Naval Research Laboratory (NRL)." 
 *         
 *  The name of NRL, the name(s) of NRL  employee(s), or any entity
 *  of the United States Government may not be used to endorse or
 *  promote  products derived from this software, nor does the 
 *  inclusion of the NRL written and developed software  directly or
 *  indirectly suggest NRL or United States  Government endorsement
 *  of this product.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * For more information send email to sdt_info@nrl.navy.mil
 *
 *
 * WWJ code:
 * 
 * Copyright (C) 2001 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 ********************************************************************/

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
