/* 
 * DISCLAIMER PLACEHOLDER 
 */

package com.ogprover.test.formats.ogp_xml;

import com.ogprover.main.OpenGeoProver;
import com.ogprover.prover_protocol.cp.OGPCP;
import com.ogprover.prover_protocol.cp.geoconstruction.Line;
import com.ogprover.prover_protocol.cp.thmstatement.TwoPerpendicularLines;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
* <dl>
* <dt><b>Class description:</b></dt>
* <dd>Class for XML converter of TwoPerpendicularLines objects</dd>
* </dl>
* 
* @version 1.00
* @author Ivan Petrovic
*/
public class TwoPerpendicularLinesConverter implements Converter {

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class clazz) {
		return clazz.equals(TwoPerpendicularLines.class);
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext ctx) {
		TwoPerpendicularLines statement = (TwoPerpendicularLines)obj;
		writer.addAttribute("line1", statement.getGeoObjects().get(0).getGeoObjectLabel());
		writer.addAttribute("line2", statement.getGeoObjects().get(1).getGeoObjectLabel());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext ctx) {
		OGPCP consProtocol = OpenGeoProver.settings.getParsedCP();
		String line1 = reader.getAttribute("line1");
		String line2 = reader.getAttribute("line2");
		
		return new TwoPerpendicularLines((Line)consProtocol.getConstructionMap().get(line1), 
				                         (Line)consProtocol.getConstructionMap().get(line2));
	}
	
}