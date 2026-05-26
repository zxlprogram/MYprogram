`timescale 1ns/1ns
module ALU1bit( a, b, cin, binvert, op, less, result, cout );
    input a, b, cin, binvert, less;
    input [1:0] op;
    output result, cout;
    wire b_inv, and_out, or_out, add_out;
    xor( b_inv, b, binvert );
    and( and_out, a, b_inv );
    or( or_out, a, b_inv );
    FullAdder fa( .a(a), .b(b_inv), .cin(cin), .sum(add_out), .cout(cout) );
    assign result = ( op == 2'b00 ) ? and_out :
                    ( op == 2'b01 ) ? or_out  :
                    ( op == 2'b10 ) ? add_out :
                    less;
endmodule
