package miniplc0java.instruction;

import java.util.Objects;

public class Instruction {
    // 描述指令的类
    private Operation opt;
    Integer x;

    public Instruction(Operation opt) {
        // 指令，包含操作和数据
        this.opt = opt;
        this.x = 0;
    }

    public Instruction(Operation opt, Integer x) {
        this.opt = opt;
        this.x = x;
    }

    public Instruction() {
        // 默认构造函数，生成非法指令
        this.opt = Operation.LIT;
        this.x = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instruction that = (Instruction) o;
        return opt == that.opt && Objects.equals(x, that.x);
    }

    @Override
    public int hashCode() {
        return Objects.hash(opt, x);
    }

    public Operation getOpt() {
        return opt;
    }

    public void setOpt(Operation opt) {
        this.opt = opt;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    @Override
    public String toString() {
        // 前面是没有操作数的指令，后面是有操作数的指令
        switch (this.opt) {
            case ADD:
            case DIV:
            case ILL:
            case MUL:
            case SUB:
            case WRT:
                return String.format("%s", this.opt);
            case LIT:
            case LOD:
            case STO:
                return String.format("%s %s", this.opt, this.x);
            default:
                return "ILL";
        }
    }
}
