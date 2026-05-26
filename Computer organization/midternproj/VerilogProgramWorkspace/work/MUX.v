`timescale 1ns/1ns
module MUX( ALUOut, HiOut, LoOut, ShifterOut, Signal, dataOut );
    input [31:0] ALUOut, HiOut, LoOut, ShifterOut;
    input [5:0] Signal;
    output [31:0] dataOut;

    assign dataOut = ( Signal == 6'b010000 ) ? HiOut      :
                     ( Signal == 6'b010010 ) ? LoOut      :
                     ( Signal == 6'b000010 ) ? ShifterOut :
                     ALUOut;
endmodule
