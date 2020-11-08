package miniplc0java.tokenizer;

import miniplc0java.error.TokenizeError;

// import java.util.Pos;
import miniplc0java.util.Pos;

import miniplc0java.error.ErrorCode;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了
    /**
     * 获取下一个 Token
     * 
     * @return
     * @throws TokenizeError 如果解析有异常则抛出
     */
    public Token nextToken() throws TokenizeError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            // 识别到文件结束符token，值为"",起始位置和终止位置都为当前位置
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar(); // 偷看下一个字符
        if (Character.isDigit(peek)) { // 如果是一个数字，那么就识别这个数字
            return lexUInt();
        } else if (Character.isAlphabetic(peek)) { // 如果是字母
            return lexIdentOrKeyword();
        } else {
            return lexOperatorOrUnknown();
        }
    }

    private Token lexUInt() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 解析存储的字符串为无符号整数
        // 解析成功则返回无符号整数类型的token，否则返回编译错误
        //
        // Token 的 Value 应填写数字的值
        // char peek = it.peekChar(); 
        var st = it.currentPos(); // 记录下开始的位置
        long t = 0;
        while(!it.isEOF())
        {
            char peek = it.peekChar();
            if(Character.isDigit(peek)){
                it.nextChar();
                t = t * 10 + peek - '0';
                // 如果超int了，就抛出异常
                if(peek > 0x7fffffff) throw new TokenizeError(ErrorCode.IntegerOverflow,it.currentPos());
            }
            else break;
        }
        // throw new Error("Not implemented");
        return new Token(TokenType.Uint,t,st,it.currentPos());
    }

    private Token lexIdentOrKeyword() throws TokenizeError {
        // 请填空：
        // 直到查看下一个字符不是数字或字母为止:
        // -- 前进一个字符，并存储这个字符
        //
        // 尝试将存储的字符串解释为关键字
        // -- 如果是关键字，则返回关键字类型的 token
        // -- 否则，返回标识符
        //
        // Token 的 Value 应填写标识符或关键字的字符串
        String value = "";
        var st = it.currentPos();
        while(!it.isEOF()&&Character.isLetterOrDigit(it.peekChar()))
        {
            value += it.peekChar();
            it.nextChar();
        }
        switch(value)
        {
            case "begin":
                return new Token(TokenType.Begin,value,st,it.currentPos());
            case "end":
                return new Token(TokenType.End,value,st,it.currentPos());
            case "var":
                return new Token(TokenType.Var,value,st,it.currentPos());
            case "const":
                return new Token(TokenType.Const,value,st,it.currentPos());
            case "print":
                return new Token(TokenType.Print,value,st,it.currentPos());
            default:
                return new Token(TokenType.Ident,value,st,it.currentPos());
        }
        
        // throw new Error("Not implemented");
    }

    private Token lexOperatorOrUnknown() throws TokenizeError {
        switch (it.nextChar()) {
            case '+':
                return new Token(TokenType.Plus, '+', it.previousPos(), it.currentPos());

            case '-':
                // 填入返回语句
                // throw new Error("Not implemented");
                return new Token(TokenType.Minus, '-', it.previousPos(), it.currentPos());

            case '*':
                // 填入返回语句
                // throw new Error("Not implemented");
                return new Token(TokenType.Mult, '*', it.previousPos(), it.currentPos());
            case '/':
                // 填入返回语句
                // throw new Error("Not implemented");
                return new Token(TokenType.Div, '/', it.previousPos(), it.currentPos());
            // 填入更多状态和返回语句
            case ';':
                return new Token(TokenType.Semicolon, ';', it.previousPos(), it.currentPos());
            case '=':
                return new Token(TokenType.Equal, '=', it.previousPos(), it.currentPos());
            case '(':
                return new Token(TokenType.LParen,'(',it.previousPos(),it.currentPos());
            case ')':
                return new Token(TokenType.RParen, ')', it.previousPos(), it.currentPos());
            default:
                // 不认识这个输入，摸了
                throw new TokenizeError(ErrorCode.InvalidInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }
}
