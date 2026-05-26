`timescale 1ns/1ns
module Multiplier( clk, reset, dataA, dataB, Signal, dataOut );
    input clk, reset;
    input [31:0] dataA, dataB;
    input [5:0] Signal;
    output [63:0] dataOut;

    parameter MULTU = 6'b011001;

    reg [63:0] product;
    reg [31:0] multiplier;
    reg [31:0] multiplicand;
    reg [5:0]  count;
    reg        running;

    assign dataOut = product;

    always @( posedge clk ) begin
        if ( reset ) begin
            product     <= 64'b0;
            multiplier  <= 32'b0;
            multiplicand<= 32'b0;
            count       <= 6'b0;
            running     <= 1'b0;
        end
        else begin
            if ( Signal == MULTU && running == 1'b0 ) begin
                product      <= 64'b0;
                multiplier   <= dataB;
                multiplicand <= dataA;
                count        <= 6'b0;
                running      <= 1'b1;
            end
            else if ( running == 1'b1 ) begin
                if ( count < 6'd32 ) begin
                    if ( multiplier[0] == 1'b1 )
                        product <= product + { 32'b0, multiplicand };
                    multiplicand <= { multiplicand[30:0], 1'b0 };
                    multiplier   <= { 1'b0, multiplier[31:1] };
                    count        <= count + 6'b1;
                end
                else begin
                    running <= 1'b0;
                end
            end
        end
    end
endmodule
