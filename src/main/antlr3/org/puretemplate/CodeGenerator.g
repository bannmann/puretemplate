/*
 * [The "BSD license"]
 *  Copyright (c) 2011 Terence Parr
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

tree grammar CodeGenerator;

options {
	language=Java;
	tokenVocab=STParser;
	ASTLabelType=CommonTree;
}

@header {
/*
 * [The "BSD license"]
 *  Copyright (c) 2011 Terence Parr
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.puretemplate;
import org.puretemplate.diagnostics.Instruction;
import org.puretemplate.error.ErrorType;
import java.util.concurrent.atomic.AtomicInteger;
}

@members {
	private String outermostTemplateName;	// name of overall template
	private CompiledST outermostImpl;
	private Token templateToken;			// overall template token
	private String template;  				// overall template text
	private ErrorManager errMgr;
	private AtomicInteger subtemplateCount = new AtomicInteger(0);

	public CodeGenerator(TreeNodeStream input, ErrorManager errMgr, String name, String template, Token templateToken) {
		this(input, new RecognizerSharedState());
		this.errMgr = errMgr;
		this.outermostTemplateName = name;
		this.template = template;
		this.templateToken = templateToken;
	}

	public void addArgument(List<FormalArgument> args, Token t) {
		String name = t.getText();
		for (FormalArgument arg : args) {
			if (arg.name.equals(name)) {
				errMgr.compileTimeError(ErrorType.PARAMETER_REDEFINITION, templateToken, t, name);
				return;
			}
		}

		args.add(new FormalArgument(name));
	}

	private String getNewSubtemplateName()
	{
		int count = subtemplateCount.incrementAndGet();
		return Compiler.SUBTEMPLATE_PREFIX + count;
	}

	// convience funcs to hide offensive sending of emit messages to
	// CompilationState temp data object.

	public void emit1(CommonTree opAST, Instruction instruction, int arg) {
		$template::state.emit1(opAST, instruction, arg);
	}
	public void emit1(CommonTree opAST, Instruction instruction, String arg) {
		$template::state.emit1(opAST, instruction, arg);
	}
	public void emit2(CommonTree opAST, Instruction instruction, int arg, int arg2) {
		$template::state.emit2(opAST, instruction, arg, arg2);
	}
	public void emit2(CommonTree opAST, Instruction instruction, String s, int arg2) {
		$template::state.emit2(opAST, instruction, s, arg2);
	}
    public void emit(CommonTree opAST, Instruction instruction) {
		$template::state.emit(opAST, instruction);
	}
	public void insert(int addr, Instruction instruction, String s) {
		$template::state.insert(addr, instruction, s);
	}
	public void setOption(CommonTree id) {
		$template::state.setOption(id);
	}
	public void write(int addr, short value) {
		$template::state.write(addr,value);
	}
	public int address() { return $template::state.ip; }
	public void func(CommonTree id) { $template::state.func(templateToken, id); }
	public void refAttr(CommonTree id) { $template::state.refAttr(templateToken, id); }
	public int defineString(String s) { return $template::state.defineString(s); }

	@Override
	public void displayRecognitionError(String[] tokenNames, RecognitionException e) {
		Token tokenWithPosition = e.token;
		if (tokenWithPosition.getInputStream() == null) {
			tokenWithPosition = input.getTreeAdaptor().getToken(input.LT(-1));
		}

		String hdr = getErrorHeader(e);
		String msg = getErrorMessage(e, tokenNames);
		errMgr.compileTimeError(ErrorType.SYNTAX_ERROR, templateToken, tokenWithPosition, hdr + " " + msg);
	}
}

templateAndEOF : template[null,null] EOF; // hush warning; ignore

template[String name, List<FormalArgument> args] returns [CompiledST impl]
scope {
    CompilationState state; // automatically get a new state pointer per invocation
}
@init {
 	$template::state = new CompilationState(errMgr, name, input.getTokenStream());
	$impl = $template::state.impl;
 	if ( $template.size() == 1 ) outermostImpl = $impl;
	$impl.defineFormalArgs($args); // make sure args are defined prior to compilation
	if ( name!=null && name.startsWith(Compiler.SUBTEMPLATE_PREFIX) ) {
	    $impl.addArg(new FormalArgument("i"));
	    $impl.addArg(new FormalArgument("i0"));
    }
	$impl.template = template; // always forget the entire template; char indexes are relative to it
}
	:	chunk
		{
        // finish off the CompiledST result
        if ( $template::state.stringtable!=null ) $impl.strings = $template::state.stringtable.toArray();
        $impl.codeSize = $template::state.ip;
		}
	;

chunk
	:	element*
	;

element
	:	^(INDENTED_EXPR INDENT compoundElement[$INDENT]) // ignore indent in front of IF and region blocks
	|	compoundElement[null]
	|	^(INDENTED_EXPR INDENT {$template::state.indent($INDENT);} singleElement? {$template::state.emit(Instruction.DEDENT);})
	|	singleElement
	;

singleElement
	:	exprElement
	|	TEXT
		{
		if ( $TEXT.text.length()>0 ) {
			emit1($TEXT,Instruction.WRITE_STR, $TEXT.text);
		}
		}

	|	NEWLINE {emit($NEWLINE, Instruction.NEWLINE);}
	;

compoundElement[CommonTree indent]
	:	ifstat[indent]
	|	region[indent]
	;

exprElement
@init { Instruction op = Instruction.WRITE; }
	:	^( EXPR expr (exprOptions {op=Instruction.WRITE_OPT;})? )
		{
		/*
		CompilationState state = $template::state;
		CompiledST impl = state.impl;
		if ( impl.instrs[state.ip-1] == Instruction.LOAD_LOCAL ) {
			impl.instrs[state.ip-1] = Instruction.WRITE_LOCAL;
		}
		else {
			emit($EXPR, op);
		}
		*/
		emit($EXPR, op);
		}
	;

region[CommonTree indent] returns [String name]
@init {
	if ( indent!=null ) $template::state.indent(indent);
}
@after {
	if ( indent!=null ) $template::state.emit(Instruction.DEDENT);
}
	:	^(	REGION ID
			{$name = STGroup.getMangledRegionName(outermostTemplateName, $ID.text);}
			template[$name,null]
			{
			CompiledST sub = $template.impl;
	        sub.isRegion = true;
	        sub.regionDefType = ST.RegionType.EMBEDDED;
	        sub.templateDefStartToken = $ID.token;
			//sub.dump();
			outermostImpl.addImplicitlyDefinedTemplate(sub);
			emit2($start, Instruction.NEW, $region.name, 0);
			emit($start, Instruction.WRITE);
			}
		 )
	;

subtemplate returns [String name, int nargs]
@init {
    $name = getNewSubtemplateName();
	List<FormalArgument> args = new ArrayList<FormalArgument>();
}
	:	^(	SUBTEMPLATE
			(^(ARGS (ID {addArgument(args, $ID.token);})+))*
			{$nargs = args.size();}
			template[$name,args]
			{
			CompiledST sub = $template.impl;
			sub.isAnonSubtemplate = true;
	        sub.templateDefStartToken = $SUBTEMPLATE.token;
            sub.ast = $SUBTEMPLATE;
            sub.ast.setUnknownTokenBoundaries();
            sub.tokens = input.getTokenStream();
			//sub.dump();
			outermostImpl.addImplicitlyDefinedTemplate(sub);
			}
		 )
	|	SUBTEMPLATE // {}
			{
			CompiledST sub = new CompiledST();
			sub.name = $name;
			sub.template = "";
			sub.addArg(new FormalArgument("i"));
			sub.addArg(new FormalArgument("i0"));
			sub.isAnonSubtemplate = true;
	        sub.templateDefStartToken = $SUBTEMPLATE.token;
            sub.ast = $SUBTEMPLATE;
            sub.ast.setUnknownTokenBoundaries();
            sub.tokens = input.getTokenStream();
			//sub.dump();
			outermostImpl.addImplicitlyDefinedTemplate(sub);
			}
	;

ifstat[CommonTree indent]
@init {
    /** Tracks address of branch operand (in code block).  It's how
     *  we backpatch forward references when generating code for IFs.
     */
    int prevBranchOperand = -1;
    /** Branch instruction operands that are forward refs to end of IF.
     *  We need to update them once we see the endif.
     */
    List<Integer> endRefs = new ArrayList<Integer>();
    if ( indent!=null ) $template::state.indent(indent);
}
@after {
	if ( indent!=null ) $template::state.emit(Instruction.DEDENT);
}
	:	^(	i='if' conditional
			{
	        prevBranchOperand = address()+1;
	        emit1($i,Instruction.BRF, -1); // write placeholder as branch target
			}
			chunk
			(	^(eif='elseif'
				{
				endRefs.add(address()+1);
				emit1($eif,Instruction.BR, -1); // br end
				// update previous branch instruction
				write(prevBranchOperand, (short)address());
				prevBranchOperand = -1;
				}
				ec=conditional
				{
		       	prevBranchOperand = address()+1;
		       	// write placeholder as branch target
		       	emit1($ec.start, Instruction.BRF, -1);
				}
				chunk
				)
			)*
			(	^(	el='else'
					{
					endRefs.add(address()+1);
					emit1($el, Instruction.BR, -1); // br end
					// update previous branch instruction
					write(prevBranchOperand, (short)address());
					prevBranchOperand = -1;
					}
					chunk
				 )
			)?
		 )
		{
		if ( prevBranchOperand>=0 ) {
			write(prevBranchOperand, (short)address());
		}
        for (int opnd : endRefs) write(opnd, (short)address());
		}
	;

conditional
	:	^(OR conditional conditional)		{emit($OR, Instruction.OR);}
	|	^(AND conditional conditional)		{emit($AND, Instruction.AND);}
	|	^(BANG conditional)					{emit($BANG, Instruction.NOT);}
	|	expr // not all expr are valid, but reuse code gen (parser restricts syntax)
	;

exprOptions : {emit($start, Instruction.OPTIONS);} ^(OPTIONS option*) ;

option : ^('=' ID expr) {setOption($ID);} ;

expr
@init {int nt = 0, ne = 0;}
	:	^(ZIP ^(ELEMENTS (expr {ne++;})+) mapTemplateRef[ne])
		{emit1($ZIP, Instruction.ZIP_MAP, ne);}
	|	^(MAP expr (mapTemplateRef[1] {nt++;})+)
		{
		if ( nt>1 ) emit1($MAP, nt>1?Instruction.ROT_MAP:Instruction.MAP, nt);
		else emit($MAP, Instruction.MAP);
		}
	|	prop
	|	includeExpr
	;

prop:	^(PROP expr ID)						{emit1($PROP, Instruction.LOAD_PROP, $ID.text);}
	|	^(PROP_IND expr expr)				{emit($PROP_IND, Instruction.LOAD_PROP_IND);}
	;

mapTemplateRef[int num_exprs]
	:	^(	INCLUDE qualifiedId
			{for (int i=1; i<=$num_exprs; i++) emit($INCLUDE,Instruction.NULL);}
			args
		)
		{
		if ( $args.passThru ) emit1($start, Instruction.PASSTHRU, $qualifiedId.text);
		if ( $args.namedArgs ) emit1($INCLUDE, Instruction.NEW_BOX_ARGS, $qualifiedId.text);
		else emit2($INCLUDE, Instruction.NEW, $qualifiedId.text, $args.n+$num_exprs);
		}
	|	subtemplate
		{
		if ( $subtemplate.nargs != $num_exprs ) {
            errMgr.compileTimeError(ErrorType.ANON_ARGUMENT_MISMATCH,
            						templateToken, $subtemplate.start.token, $subtemplate.nargs, $num_exprs);
		}
		for (int i=1; i<=$num_exprs; i++) emit($subtemplate.start,Instruction.NULL);
        emit2($subtemplate.start, Instruction.NEW,
	              $subtemplate.name,
	              $num_exprs);
		}

	|	^(	INCLUDE_IND expr
			{
			emit($INCLUDE_IND,Instruction.TOSTR);
			for (int i=1; i<=$num_exprs; i++) emit($INCLUDE_IND,Instruction.NULL);
			}
			args
			{
			emit1($INCLUDE_IND, Instruction.NEW_IND, $args.n+$num_exprs);
			}
		 )
	;

includeExpr
	:	^(EXEC_FUNC ID expr?)		{func($ID);}
	|	^(INCLUDE qualifiedId args)
		{
		if ( $args.passThru ) emit1($start, Instruction.PASSTHRU, $qualifiedId.text);
		if ( $args.namedArgs ) emit1($INCLUDE, Instruction.NEW_BOX_ARGS, $qualifiedId.text);
		else emit2($INCLUDE, Instruction.NEW, $qualifiedId.text, $args.n);
		}
	|	^(INCLUDE_SUPER ID args)
		{
		if ( $args.passThru ) emit1($start, Instruction.PASSTHRU, $ID.text);
		if ( $args.namedArgs ) emit1($INCLUDE_SUPER, Instruction.SUPER_NEW_BOX_ARGS, $ID.text);
		else emit2($INCLUDE_SUPER, Instruction.SUPER_NEW, $ID.text, $args.n);
		}
	|	^(INCLUDE_REGION ID)		{
									CompiledST impl =
										Compiler.defineBlankRegion(outermostImpl, $ID.token);
									//impl.dump();
									emit2($INCLUDE_REGION,Instruction.NEW,impl.name,0);
									}
	|	^(INCLUDE_SUPER_REGION ID)	{
		                            String mangled =
		                                STGroup.getMangledRegionName(outermostImpl.name, $ID.text);
									emit2($INCLUDE_SUPER_REGION,Instruction.SUPER_NEW,mangled,0);
									}
	|	primary
	;

primary
	:	ID				{refAttr($ID);}
	|	STRING			{emit1($STRING,Instruction.LOAD_STR, Misc.strip($STRING.text,1));}
	|	TRUE			{emit($TRUE, Instruction.TRUE);}
	|	FALSE			{emit($FALSE, Instruction.FALSE);}
	|	subtemplate		// push a subtemplate but ignore args since we can't pass any to it here
		                {emit2($start,Instruction.NEW, $subtemplate.name, 0);}
	|	list
	|	^(	INCLUDE_IND
			expr 		{emit($INCLUDE_IND, Instruction.TOSTR);}
			args        {emit1($INCLUDE_IND, Instruction.NEW_IND, $args.n);}
		 )
	|	^(TO_STR expr)	{emit($TO_STR, Instruction.TOSTR);}
	;

qualifiedId
	:	^(SLASH qualifiedId ID)
	|	^(SLASH ID)
	|	ID
	;

arg : expr ;

args returns [int n=0, boolean namedArgs=false, boolean passThru]
	:	( arg {$n++;} )+
	|	{emit($args.start, Instruction.ARGS); $namedArgs=true;}
		(	^(eq='=' ID expr)
			{$n++; emit1($eq, Instruction.STORE_ARG, defineString($ID.text));}
		)+
		( '...' {$passThru=true;} )?
    |   '...' {$passThru=true; emit($args.start, Instruction.ARGS); $namedArgs=true;}
	|
 	;

list:	{emit($start, Instruction.LIST);}
		^(LIST (listElement {emit($listElement.start, Instruction.ADD);})* )
	;

listElement : expr | NULL {emit($NULL,Instruction.NULL);} ;
