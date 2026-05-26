`timescale 1ns/1ns
module FullAdder( a, b, cin, sum, cout );
    input a, b, cin;
    output sum, cout;
    wire w1, w2, w3;
    xor( w1, a, b );
    xor( sum, w1, cin );
    and( w2, w1, cin );
    and( w3, a, b );
    or( cout, w2, w3 );
endmodule
