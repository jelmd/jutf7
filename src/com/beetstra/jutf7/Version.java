/**
 * $Id$ 
 * 
 * Copyright (c) 2006-2010 Institute of Knowledge and Language Processing (IWS),
 * Otto-von-Guericke-University Magdeburg, Department of Computer Science.
 * All Rights Reserved.
 *
 * This software is the proprietary information of the IWS.
 * Use is subject to license terms.
 */
package com.beetstra.jutf7;

/**
 * @author 	Jens Elkner
 * @version	$Revision$
 */
public class Version {
	/**
	 * Get the version info.
	 * @return version infos.
	 */
	public static final String getVersion() {
		return "@product.name@ Version @product.version@ b@build.number@";
		
	}
	
	/**
	 * Get copyright notice.
	 * @return copyright notice.
	 */
	public static final String getCopyright() {
		return "Copyright (C) @year.start@-@year.start@ J.T. Beetstra";
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(getVersion());
		System.out.println(getCopyright());
	}
}
