`timescale 1ns/1ns
module TotalALU( clk, reset, dataA, dataB, Signal, Output );
    input clk, reset;
    input [31:0] dataA, dataB;
    input [5:0] Signal;
    output [31:0] Output;

    wire [5:0]  SignaltoALU, SignaltoSHT, SignaltoMUL, SignaltoMUX;
    wire [31:0] ALUOut, HiOut, LoOut, ShifterOut;
    wire [63:0] MulAns;

    ALUControl ALUControl(
        .clk(clk), .reset(reset), .Signal(Signal),
        .SignaltoALU(SignaltoALU), .SignaltoSHT(SignaltoSHT),
        .SignaltoMUL(SignaltoMUL), .SignaltoMUX(SignaltoMUX)
    );

    ALU ALU(
        .dataA(dataA), .dataB(dataB),
        .Signal(SignaltoALU), .dataOut(ALUOut), .reset(reset)
    );

    Shifter Shifter(
        .dataA(dataA), .dataB(dataB),
        .Signal(SignaltoSHT), .dataOut(ShifterOut), .reset(reset)
    );

    Multiplier Multiplier(
        .clk(clk), .reset(reset),
        .dataA(dataA), .dataB(dataB),
        .Signal(SignaltoMUL), .dataOut(MulAns)
    );

    HiLo HiLo(
        .clk(clk), .reset(reset),
        .MulAns(MulAns), .HiOut(HiOut), .LoOut(LoOut)
    );

    MUX MUX(
        .ALUOut(ALUOut), .HiOut(HiOut), .LoOut(LoOut),
        .ShifterOut(ShifterOut), .Signal(SignaltoMUX), .dataOut(Output)
    );
endmodule
