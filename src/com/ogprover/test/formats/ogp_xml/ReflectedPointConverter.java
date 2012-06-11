/* 
 * DISCLAIMER PLACEHOLDER 
 */

package com.ogprover.test.formats.ogp_xml;

import com.ogprover.main.OpenGeoProver;
import com.ogprover.prover_protocol.cp.OGPCP;
import com.ogprover.prover_protocol.cp.geoconstruction.Line;
import com.ogprover.prover_protocol.cp.geoconstruction.Point;
import com.ogprover.prover_protocol.cp.geoconstruction.ReflectedPoint;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
* <dl>
* <dt><b>Class description:</b></dt>
* <dd>Class for XML converter of ReflexivePoint objects</dd>
* </dl>
* 
* @version 1.00
* @author Ivan Petrovic
*/
public class ReflectedPointConverter implements Converter {

	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class clazz) {
		return clazz.equals(ReflectedPoint.class);
	}

	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext ctx) {
		// obj is shortcut construction that is never present in CP in that form
		// (as single object) but as shortcut for some sequence of constructions, 
		// therefore no need to have marshal() method.
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext ctx) {
		OGPCP consProtocol = OpenGeoProver.settings.getParsedCP();
		String label = reader.getAttribute("label");
		String origpt = reader.getAttribute("origpt");
		String baseline = reader.getAttribute("baseline");
		
		return new ReflectedPoint(label, 
				                  (Point)consProtocol.getConstructionMap().get(origpt), 
				                  (Line)consProtocol.getConstructionMap().get(baseline));
	}
	
}