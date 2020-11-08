package miniplc0java.analyser;

import miniplc0java.error.AnalyzeError;
import miniplc0java.error.CompileError;
import miniplc0java.error.ErrorCode;
import miniplc0java.error.ExpectedTokenError;
import miniplc0java.error.TokenizeError;
import miniplc0java.instruction.Instruction;
import miniplc0java.instruction.Operation;
import miniplc0java.tokenizer.Token;
import miniplc0java.tokenizer.TokenType;
import miniplc0java.tokenizer.Tokenizer;
import miniplc0java.util.Pos;

import java.util.*;

public final class Analyser {

    Tokenizer tokenizer;
    ArrayList<Instruction> instructions;

    /** 当前偷看的 token */
    Token peekedToken = null;

    /** 符号表 */
    HashMap<String, SymbolEntry> symbolTable = new HashMap<>();

    /** 下一个变量的栈偏移 */
    int nextOffset = 0;

    public Analyser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.instructions = new ArrayList<>();
    }

    public List<Instruction> analyse() throws CompileError {
        analyseProgram();
        return instructions;
    }

    /**
     * 查看下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token peek() throws TokenizeError {
        if (peekedToken == null) {
            peekedToken = tokenizer.nextToken();
        }
        return peekedToken;
    }

    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError
     */
    private Token next() throws TokenizeError {
        if (peekedToken != null) {
            var token = peekedToken;
            peekedToken = null;
            return token;
        } else {
            return tokenizer.nextToken();
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则返回 true
     * 
     * @param tt
     * @return
     * @throws TokenizeError
     */
    private boolean check(TokenType tt) throws TokenizeError {
        var token = peek();
        return token.getTokenType() == tt;
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回这个 token
     * 
     * @param tt 类型
     * @return 如果匹配则返回这个 token，否则返回 null
     * @throws TokenizeError
     */
    private Token nextIf(TokenType tt) throws TokenizeError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            return null;
        }
    }

    /**
     * 如果下一个 token 的类型是 tt，则前进一个 token 并返回，否则抛出异常
     * 
     * @param tt 类型
     * @return 这个 token
     * @throws CompileError 如果类型不匹配
     */
    private Token expect(TokenType tt) throws CompileError {
        var token = peek();
        if (token.getTokenType() == tt) {
            return next();
        } else {
            throw new ExpectedTokenError(tt, token);
        }
    }

    /**
     * 获取下一个变量的栈偏移
     * 
     * @return
     */
    private int getNextVariableOffset() {
        return this.nextOffset++;
    }

    /**
     * 添加一个符号
     * 
     * @param name          名字
     * @param isInitialized 是否已赋值
     * @param isConstant    是否是常量
     * @param curPos        当前 token 的位置（报错用）
     * @throws AnalyzeError 如果重复定义了则抛异常
     */
    private void addSymbol(String name, boolean isInitialized, boolean isConstant, Pos curPos) throws AnalyzeError {
        if (this.symbolTable.get(name) != null) { // 如果已经定义，则抛出异常
            throw new AnalyzeError(ErrorCode.DuplicateDeclaration, curPos);
        } else { // 否则，加入到table里面，并且这个变量在栈上的位置就是下一个栈地址
            this.symbolTable.put(name, new SymbolEntry(isConstant, isInitialized, getNextVariableOffset()));
        }
    }

    /**
     * 设置符号为已赋值
     * 
     * @param name   符号名称
     * @param curPos 当前位置（报错用）
     * @throws AnalyzeError 如果未定义则抛异常
     */
    private void initializeSymbol(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) { // 如果符号不存在，那么就抛出异常。
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else { // 设置符号已经赋值
            entry.setInitialized(true);
        }
    }

    /**
     * 获取变量在栈上的偏移
     * 
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 栈偏移
     * @throws AnalyzeError
     */
    private int getOffset(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.getStackOffset();
        }
    }

    /**
     * 获取变量是否是常量
     * 
     * @param name   符号名
     * @param curPos 当前位置（报错用）
     * @return 是否为常量
     * @throws AnalyzeError
     */
    private boolean isConstant(String name, Pos curPos) throws AnalyzeError {
        var entry = this.symbolTable.get(name);
        if (entry == null) {
            throw new AnalyzeError(ErrorCode.NotDeclared, curPos);
        } else {
            return entry.isConstant();
        }
    }

    private void analyseProgram() throws CompileError {
        // 程序 -> 'begin' 主过程 'end'
        // 示例函数，示例如何调用子程序
        // 'begin'
        // System.out.println("查看是否是begin");
        expect(TokenType.Begin);
        // 此时已经移动到了begin的后面

        // 分析主过程
        // System.out.println("分析主过程");
        analyseMain();

        // 'end'
        // System.out.println("主过程分析结束，希望得到end");
        expect(TokenType.End);
        expect(TokenType.EOF);
    }

    private void analyseMain() throws CompileError {
        // 主过程 -> 常量声明 变量声明 语句序列
        // throw new Error("Not implemented");
        // while(peek().getTokenType()!=TokenType.End && peek().getTokenType() != TokenType.EOF)
        // {
            // 解析常量声明
            // System.out.println("解析主函数");
            analyseConstantDeclaration();
            analyseVariableDeclaration();
            analyseStatementSequence();
        // }
    }

    private void analyseConstantDeclaration() throws CompileError {
        // 示例函数，示例如何解析常量声明
        // 常量声明 -> 常量声明语句*
        // System.out.println("解析常量声明");
        // 如果下一个 token 是 const 就继续，nextIf函数指针会移动
        while (nextIf(TokenType.Const) != null) {
            // 常量声明语句 -> 'const' 变量名 '=' 常表达式 ';'

            // 常量名
            var nameToken = expect(TokenType.Ident);

            // 加入符号表
            String name = (String) nameToken.getValue();
            // System.out.println("常量名");
            // System.out.println(name);
            addSymbol(name, true, true, nameToken.getStartPos());

            // 等于号
            // System.out.println("开始检查等号");
            expect(TokenType.Equal);
            // System.out.println("成功检测到等号");
            // 常表达式
            // System.out.println("开始分析常量表达式");
            var value = analyseConstantExpression();
            // System.out.println("分析得到的值为:"+value);

            // 分号
            // System.out.println("检查分号");
            expect(TokenType.Semicolon);
            // System.out.println("成功检测到了分号");

            // 这里把常量值直接放进栈里，位置和符号表记录的一样。
            // 更高级的程序还可以把常量的值记录下来，遇到相应的变量直接替换成这个常数值，
            // 我们这里就先不这么干了。
            // load x 指令 stack[sp]=x;sp++;
            // 定义的常量直接放到栈里面，位置记录在符号表当中，且位置为下一个栈地址
            // System.out.println("添加指令");
            instructions.add(new Instruction(Operation.LIT, value));
        }
        // System.out.println("常量声明解析完毕");
    }

    private void analyseVariableDeclaration() throws CompileError {
        // 变量声明 -> 变量声明语句*
        // System.out.println("解析变量声明");
        // 如果下一个 token 是 var 就继续
        while (nextIf(TokenType.Var) != null) {
            // 变量声明语句 -> 'var' 变量名 ('=' 表达式)? ';'
            System.out.println(peek().toString());
            // 变量名
            var nameToken = expect(TokenType.Ident);
            // 变量初始化了吗
            boolean initialized = false;

            // 下个 token 是等于号吗？如果是的话分析初始化
            if(nextIf(TokenType.Equal)!=null)
            { // 如果是等号
                // 分析初始化的表达式
                // System.out.println("调用解析表达式函数");
                analyseExpression();
                // System.out.println("成功解析表达式");
                initialized = true;
                // String name = (String) nameToken.getValue();
                // addSymbol(name, true, false, nameToken.getStartPos());
            }
            // else {
            //     String name = (String) nameToken.getValue();
            //     addSymbol(name, false, false, nameToken.getStartPos());
            // }
            // System.out.println("检查分号");
            // 分号
            expect(TokenType.Semicolon);
            // System.out.println("成功检测到分号");

            // 加入符号表，请填写名字和当前位置（报错用）
            String name = (String) nameToken.getValue();
            addSymbol(name, initialized, false, /* 当前位置 */ nameToken.getStartPos());

            // 如果没有初始化的话在栈里推入一个初始值,只是为了占个位置 stack[sp]=x;sp++;
            if (!initialized) {
                instructions.add(new Instruction(Operation.LIT, 0));
            }
            // 如果初始化了的话则在分析过程当中已经将值放入了
        }
        // System.out.println("变量声明解析完毕");
    }

    private void analyseStatementSequence() throws CompileError {
        // 语句序列 -> 语句*
        // 语句 -> 赋值语句 | 输出语句 | 空语句
        // System.out.println("解析语句序列");
        while (true) {
            // 如果下一个 token 是……
            var peeked = peek();
            if (peeked.getTokenType() == TokenType.Ident) { // 这是个赋值语句
                // 调用相应的分析函数
                analyseAssignmentStatement();
                // 如果遇到其他非终结符的 FIRST 集呢？
            }
            else if(peeked.getTokenType() == TokenType.Print){ // 这是一个输出语句
                analyseOutputStatement();
            } 
            else if(peeked.getTokenType() == TokenType.Semicolon){ // 空语句
                next();
            }
            else {
                // 都不是，摸了
                break;
            }
        }
        // throw new Error("Not implemented");
        // System.out.println("语句序列解析完毕");
    }

    private int analyseConstantExpression() throws CompileError {
        // 常表达式 -> 符号? 无符号整数
        boolean negative = false;
        // System.out.println("解析表达式");
        if (nextIf(TokenType.Plus) != null) {
            // System.out.println("检查到负号");
            negative = false;
        } else if (nextIf(TokenType.Minus) != null) {
            // System.out.println("检查到正号");
            negative = true;
        }
        // System.out.println("读入整数");
        var token = expect(TokenType.Uint);
        // System.out.println("读入到的整数值为:"+token.getValue());

        long t = (long)token.getValue();
        int value = (int)t;
        if (negative) {
            value = -value;
        }
        // System.out.println("表达式结果");
        return value;
    }

    private void analyseExpression() throws CompileError {
        // 表达式 -> 项 (加法运算符 项)*
        // 项
        System.out.println("解析表达式");
        analyseItem();

        while (true) {
            // 预读可能是运算符的 token
            var op = peek();
            if (op.getTokenType() == TokenType.EOF || op.getTokenType() != TokenType.Plus && op.getTokenType() != TokenType.Minus) {
                break;
            }

            // 运算符
            next();

            // 项
            analyseItem();

            // 生成代码
            if (op.getTokenType() == TokenType.Plus) {
                instructions.add(new Instruction(Operation.ADD));
            } else if (op.getTokenType() == TokenType.Minus) {
                instructions.add(new Instruction(Operation.SUB));
            }
        }
        System.out.println("解析表达式结束");
    }

    private void analyseAssignmentStatement() throws CompileError {
        // 赋值语句 -> 标识符 '=' 表达式 ';'

        // 分析这个语句
        var ident = next();
        // 标识符是什么？
        String name = (String)ident.getValue();
        var symbol = symbolTable.get(name);
        if (symbol == null) {
            // 没有这个标识符
            throw new AnalyzeError(ErrorCode.NotDeclared, ident.getStartPos());
        } else if (symbol.isConstant) {
            // 标识符是常量
            throw new AnalyzeError(ErrorCode.AssignToConstant, ident.getStartPos());
        }
        // 设置符号已初始化
        initializeSymbol(name, ident.getStartPos());
        expect(TokenType.Equal);
        analyseExpression();
        expect(TokenType.Semicolon);
        // 把结果保存
        var offset = getOffset(name, ident.getStartPos());
        instructions.add(new Instruction(Operation.STO, offset));
    }

    private void analyseOutputStatement() throws CompileError {
        // 输出语句 -> 'print' '(' 表达式 ')' ';'

        expect(TokenType.Print);
        expect(TokenType.LParen);

        analyseExpression();

        expect(TokenType.RParen);
        expect(TokenType.Semicolon);

        instructions.add(new Instruction(Operation.WRT));
    }

    private void analyseItem() throws CompileError {
        // 项 -> 因子 (乘法运算符 因子)*

        // 因子
        System.out.println("解析项");
        analyseFactor();
        while (true) {
            // 预读可能是运算符的 token
            Token op = peek();

            // 运算符
            if(op.getTokenType() == TokenType.EOF||op.getTokenType() != TokenType.Mult && op.getTokenType() != TokenType.Div) break;
            next();
            if(op.getTokenType() == TokenType.Mult) System.out.println("*");
            else System.out.println("/");
            // 因子
            analyseFactor();
            // 生成代码
            if (op.getTokenType() == TokenType.Mult) {
                instructions.add(new Instruction(Operation.MUL));
            } else if (op.getTokenType() == TokenType.Div) {
                instructions.add(new Instruction(Operation.DIV));
            }
        }
        System.out.println("解析项结束");
    }

    private void analyseFactor() throws CompileError {
        // 因子 -> 符号? (标识符 | 无符号整数 | '(' 表达式 ')')
        System.out.println("解析因子");
        boolean negate;
        if (nextIf(TokenType.Minus) != null) { // 如果读到了负号
            System.out.println("读到了负号");
            negate = true; // 将负数标志置为true
            // 计算结果需要被 0 减
            instructions.add(new Instruction(Operation.LIT, 0));
        } else {
            nextIf(TokenType.Plus);
            negate = false;
        }

        if (check(TokenType.Ident)) {
            // 是标识符

            // 加载标识符的值
            var token = next();
            String name = (String)token.getValue();
            var symbol = symbolTable.get(name);
            System.out.println("解析到的标识符:"+name);
            if (symbol == null) {
                // 没有这个标识符
                throw new AnalyzeError(ErrorCode.NotDeclared, token.getStartPos());
            } else if (!symbol.isInitialized) {
                // 标识符没初始化
                throw new AnalyzeError(ErrorCode.NotInitialized, token.getStartPos());
            }
            var offset = getOffset(name, token.getStartPos());
            instructions.add(new Instruction(Operation.LOD, offset));
        } else if (check(TokenType.Uint)) {
            // 是整数
            // 加载整数值
            var token = next();
            long tmp = (long) token.getValue();
            int value = (int)tmp;
            System.out.println("解析到的整数:"+value);
            instructions.add(new Instruction(Operation.LIT, value));
        } else if (check(TokenType.LParen)) {
            // 是表达式
            System.out.println("(");
            next();
            analyseExpression();
            // 调用相应的处理函数
            expect(TokenType.RParen);
            System.out.println(")");
        } else {
            // 都不是，摸了
            throw new ExpectedTokenError(List.of(TokenType.Ident, TokenType.Uint, TokenType.LParen), next());
        }

        if (negate) {
            instructions.add(new Instruction(Operation.SUB));
        }
        // throw new Error("Not implemented");
        System.out.println("解析因子结束");
    }
}
