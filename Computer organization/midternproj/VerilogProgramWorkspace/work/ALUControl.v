`timescale 1ns/1ns
module ALUControl( clk, reset, Signal, SignaltoALU, SignaltoSHT, SignaltoMUL, SignaltoMUX );
    input clk, reset;
    input [5:0] Signal;
    output reg [5:0] SignaltoALU, SignaltoSHT, SignaltoMUL, SignaltoMUX;

    parameter AND   = 6'b100100;
    parameter OR    = 6'b100101;
    parameter ADD   = 6'b100000;
    parameter SUB   = 6'b100010;
    parameter SLT   = 6'b101010;
    parameter SRL   = 6'b000010;
    parameter MULTU = 6'b011001;
    parameter MFHI  = 6'b010000;
    parameter MFLO  = 6'b010010;
    parameter NOP   = 6'b111111;

    always @( posedge clk ) begin
        if ( reset ) begin
            SignaltoALU <= NOP;
            SignaltoSHT <= NOP;
            SignaltoMUL <= NOP;
            SignaltoMUX <= NOP;
        end
        else begin
            SignaltoMUX <= Signal;
            case ( Signal )
                AND: begin
                    SignaltoALU <= AND;
                    SignaltoSHT <= NOP;
                    SignaltoMUL <= NOP;
                end
                OR: begin
                    SignaltoALU <= OR;
                    SignaltoSHT <= NOP;
                    SignaltoMUL <= NOP;
                end
                ADD: begin
                    SignaltoALU <= ADD;
                    SignaltoSHT <= NOP;
                    SignaltoMUL <= NOP;
                end
                SUB: begin
                    SignaltoALU <= SUB;
                    SignaltoSHT <= NOP;
                    SignaltoMUL <= NOP;
                end
                SLT: begin
                    SignaltoALU <= SLT;
                    SignaltoSHT <= NOP;
                    SignaltoMUL <= NOP;
                end
                SRL: begin
                    SignaltoALU <= NOP;
                    SignaltoSHT <= SRL;
                    SignaltoMUL <= NOP;
                end
                MULTU: begin
                    SignaltoALU <= NOP;
                    SignaltoSHT <= NOP;
                    SignaltoMUL <= MULTU;
                end
                MFHI: begin
                    SignaltoALU <= NOP;
                    SignaltoSHT <= NOP;
                    SignaltoMUL <= NOP;
                end
                MFLO: begin
                    SignaltoALU <= NOP;
                    SignaltoSHT <= NOP;
                    SignaltoMUL <= NOP;
                end
                default: begin
                    SignaltoALU <= NOP;
                    SignaltoSHT <= NOP;
                    SignaltoMUL <= NOP;
                end
            endcase
        end
    end
endmodule
