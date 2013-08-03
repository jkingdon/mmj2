//********************************************************************/
//* Copyright (C) 2005-2011                                          */
//* MEL O'CAT  X178G243 (at) yahoo (dot) com                         */
//* License terms: GNU General Public License Version 2              */
//*                or any later version                              */
//********************************************************************/
//*4567890123456 (71-character line to adjust editor window) 23456789*/

/*
 *  Theorem.java  0.08 11/01/2011
 *
 *  5-Dec-2005: --> added isAxiom() for ProofAsst
 *
 *  Version 0.04:
 *      ==> New constructor for Theorem w/compressed proof
 *
 *  Version 0.05 Aug-27-2006:
 *      - added renderParsedSubExpr() for TMFF project
 *
 *  Version 0.06 Jun-01-2007:
 *      --> Assrt.loadDjVarsArray moved to DjVars class.
 *
 *  Version 0.07 Aug-01-2008;
 *
 *      --> Added things for TheoremLoader, including:
 *              - proofUpdates();
 *
 *  Version 0.08 - Nov-01-2011:  comment update.
 *      --> Add stmt label to ERRMSG_BAD_PARSE_STMT_1
 */

package mmj.lang;

import java.util.*;

/**
 *  Theorem corresponds to a Metamath "$p" statement,
 *  AKA, a "provable assertion".
 *  <p>
 *  Theorem is distinguished from Axiom by having a...
 *  proof, as well as an OptFrame (Optional Frame).
 *  <p>
 *  A Metamath "proof" is simply an array of Stmt labels,
 *  which in mmj equates to <code>Stmt[]</code>. The proof
 *  is in RPN, Reverse Polish Notation format and
 *  when fed into the Proof Verifier engine recreates
 *  the Theorem's Formula -- that's all a "proof" is,
 *  assuming it follows the "rules". See Metamath.pdf
 *  and mmj.verify.VerifyProofs.java for more info.
 *  <p>
 *  The proof need not be complete, and may consist of
 *  nothing but a "?" step in the source file -- which
 *  is stored as a <code>null</code> proof step in mmj.
 *  Non-missing Proof steps must refer to "active" Hyp's or
 *  Assrt's with Stmt.seq < Theorem.seq. Verification of
 *  a proof is not performed within Theorem -- that is
 *  handled by mmj.verify.VerifyProofs (in theory,
 *  Proof Verification could be performed at this time,
 *  but since most proofs are static and the Proof
 *  Verification process is very CPU intensive, normal
 *  Metamath practice is to verify proofs only when
 *  the user requests it.)
 *  <p>
 *  An OptFrame consists of those Hyp's and
 *  DjVars that are "in scope" but are not present in
 *  the the MandFrame (Mandatory Frame). Together the
 *  OptFrame and MandFrame comprise what Metamath.pdf
 *  terms the "Extended Frame".
 *  <p>
 *  (As a side note, the "validity" of a proof has nothing
 *  to do with the Theorem being reference-able by subsequent
 *  Theorems' proofs. Nor does Metamath formally require
 *  that every LogHyp in scope be referenced within a
 *  Theorem's proof. A warning message could be presented
 *  for the latter situation, but has not yet been coded.)
 *
 *  @see <a href="../../MetamathERNotes.html">
 *       Nomenclature and Entity-Relationship Notes</a>
 */
public class Theorem extends Assrt {
    private Stmt[] proof;
    private final OptFrame optFrame;

    /**
     *  Construct Theorem using the entire enchilada from
     *  mmj.mmio.SrcStmt.java, plus a few condiments.
     *
     *  @param seq          MObj.seq sequence number
     *  @param scopeDefList Scope info in effect at the time
     *  @param symTbl       Symbol Table (Map)
     *  @param stmtTbl      Statement Table (Map)
     *  @param labelS       Theorem label String
     *  @param typS         Theorem Formula Type Code String
     *  @param symList      Theorem Expression Sym String List
     *  @param proofList    Theorem Proof Stmt String List.
     *
     *  @throws LangException
     *  @see mmj.lang.Theorem#editProofListDefAndActive(
     *                          Map stmtTbl, List proofList)
     */
    public Theorem(final int seq, final List<ScopeDef> scopeDefList,
        final Map<String, Sym> symTbl, final Map<String, Stmt> stmtTbl,
        final String labelS, final String typS,
        final List<String> symList, final List<String> proofList)
        throws LangException
    {
        super(seq, scopeDefList, symTbl, stmtTbl, labelS, typS, symList);

        optFrame = buildOptFrame(scopeDefList);

        proof = editProofListDefAndActive(stmtTbl, proofList);

    }

    /**
     *  Construct Theorem using the entire enchilada from
     *  mmj.mmio.SrcStmt.java including compressed proof
     *  blocks.
     *
     *  @param seq          MObj.seq sequence number
     *  @param scopeDefList Scope info in effect at the time
     *  @param symTbl       Symbol Table (Map)
     *  @param stmtTbl      Statement Table (Map)
     *  @param labelS       Theorem label String
     *  @param typS         Theorem Formula Type Code String
     *  @param symList      Theorem Expression Sym String List
     *  @param proofList    Theorem Proof Stmt String List.
     *  @param proofBlockList  list containing one or more blocks
     *                      of compressed proof symbols.
     *  @param proofCompression instance of ProofCompression.java
     *                          used to decompress proof.
     *
     *  @throws LangException
     *  @see mmj.lang.Theorem#editProofListDefAndActive(
     *                          Map stmtTbl, List proofList)
     */
    public Theorem(final int seq, final List<ScopeDef> scopeDefList,
        final Map<String, Sym> symTbl, final Map<String, Stmt> stmtTbl,
        final String labelS, final String typS,
        final List<String> symList, final List<String> proofList,
        final List<String> proofBlockList,
        final ProofCompression proofCompression) throws LangException
    {
        super(seq, scopeDefList, symTbl, stmtTbl, labelS, typS, symList);

        optFrame = buildOptFrame(scopeDefList);

        proof = proofCompression
            .decompress(labelS, seq, stmtTbl, mandFrame.hypArray,
                optFrame.optHypArray, proofList, proofBlockList);

    }

    /**
     *  Is the Assrt an Axiom?
     *  <p>
     *  Someone asked the question, so it is answered...
     *
     *  @return true (a Theorem is not an Axiom :)
     */
    @Override
    public boolean isAxiom() {
        return false;
    }

    /**
     *  Return Theorem's proof.
     *
     *  @return Theorem's proof.
     */
    public Stmt[] getProof() {
        return proof;
    }

    /**
     *  Set Theorem's proof.
     *
     *  Updates the Theorem's proof, but only if the proof
     *  is valid enough to keep -- not saying it is a
     *  "valid proof" though :)
     *
     *  @param  stmtTbl    Statement Table (Map)
     *  @param  proofList Theorem Proof Stmt String List.
     *  @return Theorem's proof.
     *  @throws LangException
     *  @see mmj.lang.Theorem#editProofListDefAndActive(
     *                          Map stmtTbl, List proofList)
     */
    public Stmt[] setProof(final Map<String, Stmt> stmtTbl,
        final List<String> proofList) throws LangException
    {
        proof = editProofListDefAndActive(stmtTbl, proofList);
        return proof;
    }

    /**
     *  Get the Theorem's OptFrame.
     *  <p>
     *  @return OptFrame for Theorem.
     */
    public OptFrame getOptFrame() {
        return optFrame;
    }

    /**
     *  Validates each proof step for a proof being loaded
     *  into mmj.
     *  <p>
     *  <ol>
     *  <li>refers to an existing statement label -- or is "?", in
     *      which case we convert it to "null".
     *  <li>make sure existing statement label's seq nbr < this
     *      stmt seq.
     *      (this feature has no use when reading a file but when we
     *       are adding proofs to an existing system we need to make
     *       sure we are referring only to earlier statements in the
     *       database).
     *  <li>the proof steps are converted from label strings to
     *      actual object references to existing statements in
     *      <code>stmtTbl</code> -- except for missing steps
     *      denoted by "?", which are converted to "null".
     *  <li>verify that a step referring to a hypothesis refers to
     *      either an active hypothesis or an hypothesis in the
     *      MandFrame or OptFrame (called "Extended Frame" in
     *      metamath.pdf.) Note: checking the Frame applies only
     *      when a proof is being updated after the source file
     *      has already been loaded -- out of scope, "inactive"
     *      hypotheses will be in the Frame if they are available
     *      for use by the proof!
     *  <li>as a double-check, if the proofList is empty,
     *      raise an exception.
     *  <li>build the proof array and return it to the caller.
     *  </ol>
     *
     * @param stmtTbl  Map of statements already loaded.
     *
     * @param proofList  Theorem's proof steps, the labels (Strings)
     *        of existing Stmt's (or "?" for missing step(s)).
     *
     * @return Stmt[]  an array of proof steps; contains null entries
     *        for input proof steps specifying "?" (unknown/missing).
     *
     * @throws    LangException
     *            (see <code>mmj.lang.LangConstants.java</code>)
     */
    public Stmt[] editProofListDefAndActive(final Map<String, Stmt> stmtTbl,
        final List<String> proofList) throws LangException
    {

        String stepS;
        Stmt stepTbl;
        final int stepCount = proofList.size();
        final int theoremSeq = getSeq();

        if (stepCount < 1)
            throw new LangException(LangConstants.ERRMSG_PROOF_HAS_NO_STEPS);

        final Stmt[] proofArray = new Stmt[stepCount];

        for (int i = 0; i < stepCount; i++) {
            stepS = proofList.get(i);
            if (stepS.equals(LangConstants.MISSING_PROOF_STEP))
                proofArray[i] = null;
            else {
                stepTbl = stmtTbl.get(stepS);
                if (stepTbl == null)
                    throw new LangException(
                        LangConstants.ERRMSG_PROOF_STEP_LABEL_NOTFND + stepS);
                if (stepTbl.seq >= theoremSeq)
                    throw new LangException(
                        LangConstants.ERRMSG_FORWARD_PROOF_STEP_LABEL + stepS
                            + LangConstants.ERRMSG_FORWARD_PROOF_STEP_LABEL_2
                            + getLabel());
                if (stepTbl.isHyp())
                    /**
                     *  this is a little "tricky" -- "active" applies
                     *  only to global hypotheses or when the source
                     *  file is being loaded.
                     */
                    if (stepTbl.isActive()) {}
                    else if (isProofStepInExtendedFrame(stepTbl)) {}
                    else
                        throw new LangException(
                            LangConstants.ERRMSG_PROOF_STEP_HYP_INACTIVE
                                + stepS);
                proofArray[i] = stepTbl;
            }
        }
        return proofArray;
    }

    /**
     *  Checks to see whether or not a proof step is
     *  contained in the Theorem's Extended Frame.
     *  <p>
     *  First checks to see if the proof step is in the
     *  MandFrame's hypArray. If not it checks the
     *  OptFrame's hypArray
     *
     *  @param proofStep a Statement reference.
     *
     *  @return true if proof step == a Hyp in either the
     *   MandFrame or OptFrame of the Theorem.
     */
    public boolean isProofStepInExtendedFrame(final Stmt proofStep) {
        for (final Hyp element : mandFrame.hypArray)
            if (proofStep == element)
                return true;
        for (final Hyp element : optFrame.optHypArray)
            if (proofStep == element)
                return true;
        return false;
    }

    /**
     *  Constructs and loads an OptFrame consisting of all
     *  Hyp and DjVars that are "in scope" but are not
     *  already contained in the Theorem's MandFrame.
     *  <p>
     *  <ol>
     *  <li>build optHypArray consisting of all variable
     *      hypotheses in scope that are not present in
     *      (the mandatory) hypArray.
     *  <li>build optDjVarsArray consisting of all DjVars
     *      restrictions in scope that are not already
     *      present in (the mandatory) djVarsArray.
     *  </ol>
     *
     * @param scopeDefList List containing all presently
     *        active ScopeDef's.
     *
     * @return OptFrame -- Optional variable hypotheses and disjoint
     *                     variable restrictions.
     */
    private OptFrame buildOptFrame(final List<ScopeDef> scopeDefList) {
        final OptFrame oF = new OptFrame();
        final List<Hyp> optHypList = new ArrayList<Hyp>();

        for (final ScopeDef scopeDef : scopeDefList)
            for (final VarHyp varHyp : scopeDef.scopeVarHyp)
                if (!isHypInMandHypArray(varHyp))
                    accumHypInList(optHypList, varHyp);

        // could not get this to compile.?!
        // oF.optHypArray = optHypList.toArray(oF.optHypArray);
        oF.optHypArray = optHypList.toArray(new Hyp[optHypList.size()]);

        final List<DjVars> optDjVarsList = new ArrayList<DjVars>();
        for (final ScopeDef scopeDef : scopeDefList)
            for (final DjVars djVars : scopeDef.scopeDjVars)
                if (!optDjVarsList.contains(djVars)
                    && !MandFrame.isVarPairInDjArray(mandFrame,
                        djVars.getVarLo(), djVars.getVarHi()))
                    optDjVarsList.add(djVars);

        oF.optDjVarsArray = optDjVarsList.toArray(new DjVars[optDjVarsList
            .size()]);

        return oF;
    }

    /**
     *  Throws an IllegalArgumentException because a
     *  ParseTree for a parsed sub-expression should
     *  contain only VarHyp and Syntax Axiom nodes.
     *  <p>
     *  @param sb            StringBuilder already initialized
     *                       for appending characters.
     *
     *  @param maxDepth      maximum depth of Notation Syntax
     *                       axioms in sub-tree to be printed.
     *                       Set to Integer.MAX_VALUE to turn
     *                       off depth checking.
     *
     *  @param maxLength     maximum length of output
     *                       sub-expression.
     *                       Set to Integer.MAX_VALUE to turn
     *                       off depth checking.
     *
     *  @param child         array of ParseNode, corresponding
     *                       to VarHyp nodes to be substituted
     *                       into the Stmt.
     *
     *  @return length of sub-expression characters
     *          appended to the input StringBuilder --
     *          or -1 if maxDepth or maxLength exceeded.
     */
    @Override
    public int renderParsedSubExpr(final StringBuilder sb, final int maxDepth,
        final int maxLength, final ParseNode[] child)
    {

        throw new IllegalArgumentException(
            LangConstants.ERRMSG_BAD_PARSE_STMT_1 + getLabel()
                + LangConstants.ERRMSG_BAD_PARSE_STMT_2);

        // return -1;
    }

    /**
     *  Applies updates made to proof related data.
     *  <p>
     *  @param newProof previous proof.
     *  @param newDjVarsArray previous Mandatory DjVars
     *  @param newOptDjVarsArray previous Optional DjVars
     *
     */
    public void proofUpdates(final Stmt[] newProof,
        final DjVars[] newDjVarsArray, final DjVars[] newOptDjVarsArray)
    {

        proof = newProof;
        mandFrame.djVarsArray = newDjVarsArray;
        optFrame.optDjVarsArray = newOptDjVarsArray;
    }

    /**
     *  Utility routine for Theorem Loader to split an input
     *  list of Distinct Variable statement symbol lists
     *  into Mandatory and Optional DjVars lists.
     *  <p>
     *  Will throw LangException if both variables not
     *  contained in Mandatory or Optional Frame hyp arrays!
     *  <p>
     *  @param symTbl Symbol Table from Logical System.
     *
     *  @param inputDjVarsStmtList List of lists where
     *             the inner list is symList from SrcStmt.
     *
     *  @param mandDjVarsUpdateList empty list upon input to
     *             contain the Mandatory DjVars generated
     *             from the inputDjVarsStmtList.
     *
     *  @param optDjVarsUpdateList empty list upon input to
     *             contain the Optional DjVars generated
     *             from the inputDjVarsStmtList.
     *
     *  @throws LangException if validation error.
     *
     */
    public void loadMandAndOptDjVarsUpdateLists(final Map<String, Sym> symTbl,
        final List<List<String>> inputDjVarsStmtList,
        final List<DjVars> mandDjVarsUpdateList,
        final List<DjVars> optDjVarsUpdateList) throws LangException
    {

        for (final List<String> symList : inputDjVarsStmtList) {
            final Var[] varArray = new Var[symList.size()];
            for (int j = 0; j < varArray.length; j++)
                varArray[j] = Var.verifyVarDef(symTbl, symList.get(j));
            for (int m = 0; m < varArray.length - 1; m++)
                for (int n = m + 1; n < varArray.length; n++) {
                    final DjVars djVars = new DjVars(varArray[m], varArray[n]);
                    if (mandFrame.areBothDjVarsInHypArray(djVars))
                        mandDjVarsUpdateList.add(djVars);
                    else if (DjVars.areBothDjVarsInExtendedFrame(djVars,
                        mandFrame, optFrame))
                        optDjVarsUpdateList.add(djVars);
                    else
                        throw new LangException(
                            LangConstants.ERRMSG_DJ_VARS_VARS_NOT_DEF_IN_EXT_FRAME
                                + djVars.toString());
                }
        }
    }

    /**
     *  Replace existing Mandatory and Optional DjVars.
     *  <p>
     *  @param mandDjVarsUpdateList List of DjVars object
     *         to be stored.
     *  @param optDjVarsUpdateList List of DjVars object
     *         to be stored.
     */
    public void replaceDjVars(final List<DjVars> mandDjVarsUpdateList,
        final List<DjVars> optDjVarsUpdateList)
    {

        mandFrame.djVarsArray = DjVars
            .sortAndCombineDvArrays(null, mandDjVarsUpdateList
                .toArray(new DjVars[mandDjVarsUpdateList.size()]));

        optFrame.optDjVarsArray = DjVars
            .sortAndCombineDvArrays(null, optDjVarsUpdateList
                .toArray(new DjVars[optDjVarsUpdateList.size()]));
    }

    /**
     *  Merge existing Mandatory and Optional DjVars into
     *  the existing Mandatory and Optional Frame data.
     *  <p>
     *  @param mandDjVarsUpdateList List of DjVars object
     *         to be stored.
     *  @param optDjVarsUpdateList List of DjVars object
     *         to be stored.
     */
    public void mergeDjVars(final List<DjVars> mandDjVarsUpdateList,
        final List<DjVars> optDjVarsUpdateList)
    {

        mandFrame.djVarsArray = DjVars.sortAndCombineDvArrays(
            mandFrame.djVarsArray, mandDjVarsUpdateList
                .toArray(new DjVars[mandDjVarsUpdateList.size()]));

        optFrame.optDjVarsArray = DjVars
            .sortAndCombineDvArrays(optFrame.optDjVarsArray,
                optDjVarsUpdateList.toArray(new DjVars[optDjVarsUpdateList
                    .size()]));
    }

    private boolean isHypInMandHypArray(final Hyp hyp) {
        for (final Hyp element : mandFrame.hypArray)
            if (hyp == element)
                return true;
        return false;
    }

}
