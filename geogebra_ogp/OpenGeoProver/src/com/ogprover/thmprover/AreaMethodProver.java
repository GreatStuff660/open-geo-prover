/* 
 * DISCLAIMER PLACEHOLDER 
 */

package com.ogprover.thmprover;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import com.ogprover.main.OpenGeoProver;
import com.ogprover.pp.tp.OGPTP;
import com.ogprover.pp.tp.auxiliary.UnknownStatementException;
import com.ogprover.pp.tp.expressions.AMExpression;
import com.ogprover.pp.tp.expressions.Fraction;
import com.ogprover.pp.tp.expressions.BasicNumber;
import com.ogprover.pp.tp.expressions.Product;
import com.ogprover.pp.tp.geoconstruction.AMFootPoint;
import com.ogprover.pp.tp.geoconstruction.AMIntersectionPoint;
import com.ogprover.pp.tp.geoconstruction.FreePoint;
import com.ogprover.pp.tp.geoconstruction.GeoConstruction;
import com.ogprover.pp.tp.geoconstruction.PRatioPoint;
import com.ogprover.pp.tp.geoconstruction.Point;
import com.ogprover.pp.tp.ndgcondition.SimpleNDGCondition;
import com.ogprover.pp.tp.thmstatement.AreaMethodTheoremStatement;
import com.ogprover.utilities.logger.ILogger;

/**
 * <dl>
 * <dt><b>Class description:</b></dt>
 * <dd>Class for the area method prover</dd>
 * </dl>
 * 
 * @version 1.00
 * @author Damien Desfontaines
 */
public class AreaMethodProver implements TheoremProver {
	/*
	 * ======================================================================
	 * ========================== VARIABLES =================================
	 * ======================================================================
	 */
	/**
	 * Known results.
	 */
	private static HashMap<AreaMethodTheoremStatement, Boolean> alreadyProvedStatements;
	
	/**
	 * Triples of known collinear points.
	 */
	private static HashSet<HashSet<Point>> knownCollinearPoints;
	 
	/**
	 * Statement to be proved
	 */
	protected AreaMethodTheoremStatement statement;
	
	/**
	 * Steps of the construction
	 */
	protected Vector<GeoConstruction> constructions;
	
	/**
	 * Index of the next point to eliminate
	 */
	protected int nextPointToEliminate;
	
	/**
	 * Steps of the computation (we store them for future printing)
	 */
	protected Vector<AMExpression> steps;
	
	/**
	 * NDGs conditions of the theorem
	 */
	protected Vector<SimpleNDGCondition> ndgConditions;
	
	
	static {
		alreadyProvedStatements = new HashMap<AreaMethodTheoremStatement, Boolean>();
	}
	
	/*
	 * ======================================================================
	 * ========================== GETTERS/SETTERS ===========================
	 * ======================================================================
	 */
	public AreaMethodTheoremStatement getStatement() {
		return statement;
	}
	
	public Vector<GeoConstruction> getConstructions() {
		return constructions;
	}
	
	public Vector<SimpleNDGCondition> getNDGConditions() {
		return ndgConditions;
	}
	
	
	
	/*
	 * ======================================================================
	 * ========================== CONSTRUCTORS ==============================
	 * ======================================================================
	 */
	/**
	 * Constructor method
	 * @param thmProtocol	The details of the construction, with the type OGPTP.
	 */
	public AreaMethodProver(OGPTP thmProtocol) {
		this.steps = new Vector<AMExpression>();
		this.statement = thmProtocol.getTheoremStatement().getAreaMethodStatement();
		this.constructions = thmProtocol.getConstructionSteps();
		this.nextPointToEliminate = constructions.size()-1;
		this.ndgConditions = thmProtocol.getSimpleNDGConditions();
		computeNextPointToEliminate();
	}
	
	/**
	 * Constructor method
	 * @param statement			The statement to prove
	 * @param constructions		The details of the construction
	 * @param ndgConditions		The NDGs-conditions
	 */
	public AreaMethodProver(AreaMethodTheoremStatement statement, Vector<GeoConstruction> constructions, Vector<SimpleNDGCondition> ndgConditions) {
		this.steps = new Vector<AMExpression>();
		this.statement = statement;
		this.constructions = constructions;
		this.nextPointToEliminate = constructions.size()-1;
		this.ndgConditions = ndgConditions;
		computeNextPointToEliminate();
	}

	
	/*
	 * ======================================================================
	 * ========================== SPECIFIC METHODS ==========================
	 * ======================================================================
	 */
	public int prove() {
		ILogger logger = OpenGeoProver.settings.getLogger();
		
		if (alreadyProvedStatements.containsKey(statement)) {
			debug("Statement to prove : " + statement.getName());
			if (alreadyProvedStatements.get(statement).booleanValue()) {
				debug("-> We have already proved that it was true");
				return THEO_PROVE_RET_CODE_TRUE;
			}
			debug("-> We have already proved that it was false");
			return THEO_PROVE_RET_CODE_FALSE;
		}
		
		debug("Description of the intern representation of the construction :");
		for (GeoConstruction cons : constructions) {
			debug("  " + cons.getConstructionDesc());
		}
		
		debug("Description of the NDGs conditions :");
		if (ndgConditions != null) {
			for (SimpleNDGCondition ndgCons : ndgConditions) {
				debug("  " + ndgCons.print());
			}
		}
		
		debug("Statement to prove " + statement.getName());
		
		if (statement == null) {
			logger.error("Statement is null");
			return TheoremProver.THEO_PROVE_RET_CODE_UNKNOWN;
		}
		
		debug("Number of expressions in the statement : " + Integer.toString(statement.getStatements().size()));
		
		computeCollinearPoints();
		
		for (AMExpression expr : statement.getStatements()) {
			debug("We must prove that : " + expr.print() + " = 0");
			steps.add(expr);
			AMExpression current = expr;
			computeNextPointToEliminate();
			while (nextPointToEliminate >=0  && !current.isZero()) {
				//if (current.containsOnlyFreePoints()) {
					//
					// The current expression, after uniformization and simplification, is non-zero 
					// but contains only free points : it is maybe false, but it can also be an 
					// expression such as S_ABC = S_ABD + S_ADC + S_DBC (which is always true). In this
					// case, we have to transform it with the area coordinates. 
					//
					// TODO implement this
				//	return TheoremProver.THEO_PROVE_RET_CODE_UNKNOWN;
				//}
				debug("Removing the areas which trivially equal zero of : ", current);
				current = current.simplifyCollinearPoints(knownCollinearPoints);
				debug("Uniformization of : ", current);
				current = current.uniformize();
				debug("Simplification of : ", current);
				current = current.simplify();
				String label = constructions.get(nextPointToEliminate).getGeoObjectLabel();
				debug("Removing the point " + label + " of the formula : ", current);
				try {
					current = current.eliminate((Point)constructions.get(nextPointToEliminate), this); //safe cast
				} catch (UnknownStatementException e) {
					logger.error("The point elimination required a intermediary lemma to be proved, and the sub-process crashed.");
					logger.error("It occured on : " + e.getMessage());
					return TheoremProver.THEO_PROVE_RET_CODE_UNKNOWN;
				}
				nextPointToEliminate--;
				computeNextPointToEliminate();
				debug("Removing the areas which trivially equal zero of : ", current);
				current = current.simplifyCollinearPoints(knownCollinearPoints);
				debug("Second uniformization of : ", current);
				current = current.uniformize();
				debug("Second simplification of : ", current);
				current = current.simplify();
				debug("Reducing into a single fraction of : ", current);
				current = current.reduceToSingleFraction();
				if (current instanceof Fraction) {
					debug("Removing of the denominator of : ", current);
					current = ((Fraction) current).getNumerator();
				}
				debug("Last simplification of : ", current);
				current = current.simplify();
				debug("Reducing into a right associative form of : ", current);
			}
			debug("Reducing into a single fraction of : ", current);
			current = current.reduceToSingleFraction();
			if (current instanceof Fraction) {
				debug("Removing of the denominator of : ", current);
				current = ((Fraction) current).getNumerator();
			}
			debug("Last simplification of : ", current);
			current = current.simplify();
			debug("Reducing into a right associative form of : ", current);
			current = (new Product(new BasicNumber(1), current)).reduceToRightAssociativeForm();
			debug("Grouping of : ", current);
			current = current.groupSumOfProducts();
			debug("Simplification of : ", current);
			current = current.simplify();
			if (!(current.isZero())) {
				debug("Transformation to a formula with only independant variables of : ", current);
				try {
					current = current.toIndependantVariables(this);
				} catch (UnknownStatementException e) {
					logger.error("The transformation to a formula with independant variables" +
							" required a intermediary lemma to be proved, and the sub-process crashed.");
					logger.error("It occured on : " + e.getMessage());
					return TheoremProver.THEO_PROVE_RET_CODE_UNKNOWN;
				}
				debug("Uniformization of : ", current);
				current = current.uniformize();
				debug("Simplification of : ", current);
				current = current.simplify();
				debug("Reducing into a single fraction of : ", current);
				current = current.reduceToSingleFraction();
				if (current instanceof Fraction) {
					debug("Removing of the denominator of : ", current);
					current = ((Fraction) current).getNumerator();
				}
				debug("Simplification of : ", current);
				current = current.simplify();
				debug("Reducing into a right associative form of : ", current);
				current = (new Product(new BasicNumber(1), current)).reduceToRightAssociativeForm();
				debug("Grouping of : ", current);
				current = current.groupSumOfProducts();
				debug("Simplification of : ", current);;
				current = current.simplify();
				debug("Result : ", current);
				if (current.isZero())
					debug("The formula equals zero : the statement is then proved");
				else
					return TheoremProver.THEO_PROVE_RET_CODE_FALSE;
			}
		}
		
		return TheoremProver.THEO_PROVE_RET_CODE_TRUE;
	}
	
	private void computeNextPointToEliminate() {
		while (nextPointToEliminate >= 0
				&& (!(constructions.get(nextPointToEliminate) instanceof Point) 
						|| constructions.get(nextPointToEliminate) instanceof FreePoint))
			nextPointToEliminate--;
	}
	
	private static void debug(String str, AMExpression expr) {
		ILogger logger = OpenGeoProver.settings.getLogger();
		int size = expr.size();
		int MAX_SIZE = 200;
		if (size >= MAX_SIZE)
			logger.debug(str + "Too large to be printed");
		else
			logger.debug(str + expr.print());
		logger.debug("  (Size = " + Integer.toString(size) + ")");
	}
	
	private static void debug(String str)  {
		ILogger logger = OpenGeoProver.settings.getLogger();
		logger.debug(str);
	}
	
	private void computeCollinearPoints() {
		if (knownCollinearPoints != null)
			return;
		
		knownCollinearPoints = new HashSet<HashSet<Point>>();
		
		int numberOfConstructions = constructions.size();
		for (int i = 0 ; i < numberOfConstructions ; i++) {
			for (int j = 0 ; j < numberOfConstructions ; j++) {
				for (int k = 0 ; k < numberOfConstructions ; k++) {
					if (constructions.get(k) instanceof Point && !(constructions.get(k) instanceof FreePoint) ) {
						Point current = (Point)constructions.get(k);
						if (current instanceof AMIntersectionPoint) {
							HashSet<Point> set = new HashSet<Point>();
							set.add(((AMIntersectionPoint) current).getU());
							set.add(((AMIntersectionPoint) current).getV());
							set.add(current);
							knownCollinearPoints.add(set);
							set = new HashSet<Point>();
							set.add(((AMIntersectionPoint) current).getP());
							set.add(((AMIntersectionPoint) current).getQ());
							set.add(current);
							knownCollinearPoints.add(set);
						} else if (current instanceof AMFootPoint) {
							HashSet<Point> set = new HashSet<Point>();
							set.add(((AMFootPoint) current).getU());
							set.add(((AMFootPoint) current).getV());
							set.add(current);
							knownCollinearPoints.add(set);
						} else if (current instanceof PRatioPoint) {
							Point w = ((PRatioPoint) current).getW();
							Point u = ((PRatioPoint) current).getU();
							Point v = ((PRatioPoint) current).getV();
							HashSet<Point> wuv = new HashSet<Point>();
							wuv.add(w); wuv.add(u); wuv.add(v);
							if (w.equals(u) || w.equals(v) || knownCollinearPoints.contains(wuv)) {
								HashSet<Point> set = new HashSet<Point>();
								set.add(u); set.add(v); set.add(current);
								knownCollinearPoints.add(set);
								set = new HashSet<Point>();
								set.add(u); set.add(w); set.add(current);
								knownCollinearPoints.add(set);
								set = new HashSet<Point>();
								set.add(v); set.add(w); set.add(current);
								knownCollinearPoints.add(set);
							}
						}
					}
				}
			}
		}
	}
}