`timescale 1ns/1ns
module HiLo( clk, reset, MulAns, HiOut, LoOut );
    input clk, reset;
    input [63:0] MulAns;
    output [31:0] HiOut, LoOut;

    reg [63:0] HiLoReg;

    always @( posedge clk ) begin
        if ( reset )
            HiLoReg <= 64'b0;
        else
            HiLoReg <= MulAns;
    end

    assign HiOut = HiLoReg[63:32];
    assign LoOut = HiLoReg[31:0];
endmodule
