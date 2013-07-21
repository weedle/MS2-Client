package com.mineshaftersquared.misc;

import java.util.ListIterator;

import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.ParseException;

public class ExtendedGnuParser extends GnuParser {
	
	@Override
	protected void processOption(String opt, ListIterator iter) throws ParseException {
		if (this.getOptions().hasOption(opt)) {
			super.processOption(opt, iter);
		}
	}
}
