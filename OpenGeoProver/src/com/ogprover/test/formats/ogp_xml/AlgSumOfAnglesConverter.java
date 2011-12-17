/* 
 * DISCLAIMER PLACEHOLDER 
 */

package com.ogprover.test.formats.ogp_xml;

import com.ogprover.prover_protocol.cp.auxiliary.Angle;
import com.ogprover.prover_protocol.cp.thmstatement.AlgebraicSumOfThreeAngles;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
* <dl>
* <dt><b>Class description:</b></dt>
* <dd>Class for XML converter of AlgebraicSumOfThreeAngles objects</dd>
* </dl>
* 
* @version 1.00
* @author Ivan Petrovic
*/
public class AlgSumOfAnglesConverter implements Converter {

	@SuppressWarnings("rawtypes")
	public boolean canConvert(Class clazz) {
		return clazz.equals(AlgebraicSumOfThreeAngles.class);
	}

	public void marshal(Object obj, HierarchicalStreamWriter writer,
			MarshallingContext ctx) {
		AlgebraicSumOfThreeAngles sum = (AlgebraicSumOfThreeAngles)obj;
		Angle firstAng = sum.getFirstAngle();
		Angle secondAng = sum.getSecondAngle();
		Angle thirdAng = sum.getThirdAngle();
		
		writer.startNode("angle");
		ctx.convertAnother(firstAng);
		writer.endNode();
		writer.startNode("angle");
		ctx.convertAnother(secondAng);
		writer.endNode();
		writer.startNode("angle");
		ctx.convertAnother(thirdAng);
		writer.endNode();
	}

	public Object unmarshal(HierarchicalStreamReader reader,
			UnmarshallingContext ctx) {
		if (!reader.hasMoreChildren())
			return null;
		
		reader.moveDown();
		if (!"angle".equals(reader.getNodeName()))
			return null;
		Angle firstAng = (Angle)ctx.convertAnother(null, Angle.class);
		reader.moveUp();
		
		if (!reader.hasMoreChildren())
			return null;
		
		reader.moveDown();
		if (!"angle".equals(reader.getNodeName()))
			return null;
		Angle secondAng = (Angle)ctx.convertAnother(null, Angle.class);
		reader.moveUp();
		
		if (!reader.hasMoreChildren())
			return null;
		
		reader.moveDown();
		if (!"angle".equals(reader.getNodeName()))
			return null;
		Angle thirdAng = (Angle)ctx.convertAnother(null, Angle.class);
		reader.moveUp();

		return new AlgebraicSumOfThreeAngles(firstAng, secondAng, thirdAng);
	}
	
}