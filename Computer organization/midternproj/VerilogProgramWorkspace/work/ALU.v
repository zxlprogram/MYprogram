`timescale 1ns/1ns
module ALU( dataA, dataB, Signal, dataOut, reset );
    input reset;
    input [31:0] dataA, dataB;
    input [5:0] Signal;
    output [31:0] dataOut;

    wire binvert;
    wire [1:0] op;
    wire [31:0] carry, res;
    wire set, cout_final;

    assign binvert = ( Signal == 6'b100010 || Signal == 6'b101010 ) ? 1'b1 : 1'b0;
    assign op = ( Signal == 6'b100100 ) ? 2'b00 :
                ( Signal == 6'b100101 ) ? 2'b01 :
                ( Signal == 6'b100000 ) ? 2'b10 :
                ( Signal == 6'b100010 ) ? 2'b10 :
                ( Signal == 6'b101010 ) ? 2'b11 :
                2'b10;

    ALU1bit alu0 ( .a(dataA[0]),  .b(dataB[0]),  .cin(binvert),    .binvert(binvert), .op(op), .less(set), .result(res[0]),  .cout(carry[0])  );
    ALU1bit alu1 ( .a(dataA[1]),  .b(dataB[1]),  .cin(carry[0]),   .binvert(binvert), .op(op), .less(1'b0), .result(res[1]),  .cout(carry[1])  );
    ALU1bit alu2 ( .a(dataA[2]),  .b(dataB[2]),  .cin(carry[1]),   .binvert(binvert), .op(op), .less(1'b0), .result(res[2]),  .cout(carry[2])  );
    ALU1bit alu3 ( .a(dataA[3]),  .b(dataB[3]),  .cin(carry[2]),   .binvert(binvert), .op(op), .less(1'b0), .result(res[3]),  .cout(carry[3])  );
    ALU1bit alu4 ( .a(dataA[4]),  .b(dataB[4]),  .cin(carry[3]),   .binvert(binvert), .op(op), .less(1'b0), .result(res[4]),  .cout(carry[4])  );
    ALU1bit alu5 ( .a(dataA[5]),  .b(dataB[5]),  .cin(carry[4]),   .binvert(binvert), .op(op), .less(1'b0), .result(res[5]),  .cout(carry[5])  );
    ALU1bit alu6 ( .a(dataA[6]),  .b(dataB[6]),  .cin(carry[5]),   .binvert(binvert), .op(op), .less(1'b0), .result(res[6]),  .cout(carry[6])  );
    ALU1bit alu7 ( .a(dataA[7]),  .b(dataB[7]),  .cin(carry[6]),   .binvert(binvert), .op(op), .less(1'b0), .result(res[7]),  .cout(carry[7])  );
    ALU1bit alu8 ( .a(dataA[8]),  .b(dataB[8]),  .cin(carry[7]),   .binvert(binvert), .op(op), .less(1'b0), .result(res[8]),  .cout(carry[8])  );
    ALU1bit alu9 ( .a(dataA[9]),  .b(dataB[9]),  .cin(carry[8]),   .binvert(binvert), .op(op), .less(1'b0), .result(res[9]),  .cout(carry[9])  );
    ALU1bit alu10( .a(dataA[10]), .b(dataB[10]), .cin(carry[9]),   .binvert(binvert), .op(op), .less(1'b0), .result(res[10]), .cout(carry[10]) );
    ALU1bit alu11( .a(dataA[11]), .b(dataB[11]), .cin(carry[10]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[11]), .cout(carry[11]) );
    ALU1bit alu12( .a(dataA[12]), .b(dataB[12]), .cin(carry[11]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[12]), .cout(carry[12]) );
    ALU1bit alu13( .a(dataA[13]), .b(dataB[13]), .cin(carry[12]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[13]), .cout(carry[13]) );
    ALU1bit alu14( .a(dataA[14]), .b(dataB[14]), .cin(carry[13]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[14]), .cout(carry[14]) );
    ALU1bit alu15( .a(dataA[15]), .b(dataB[15]), .cin(carry[14]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[15]), .cout(carry[15]) );
    ALU1bit alu16( .a(dataA[16]), .b(dataB[16]), .cin(carry[15]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[16]), .cout(carry[16]) );
    ALU1bit alu17( .a(dataA[17]), .b(dataB[17]), .cin(carry[16]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[17]), .cout(carry[17]) );
    ALU1bit alu18( .a(dataA[18]), .b(dataB[18]), .cin(carry[17]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[18]), .cout(carry[18]) );
    ALU1bit alu19( .a(dataA[19]), .b(dataB[19]), .cin(carry[18]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[19]), .cout(carry[19]) );
    ALU1bit alu20( .a(dataA[20]), .b(dataB[20]), .cin(carry[19]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[20]), .cout(carry[20]) );
    ALU1bit alu21( .a(dataA[21]), .b(dataB[21]), .cin(carry[20]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[21]), .cout(carry[21]) );
    ALU1bit alu22( .a(dataA[22]), .b(dataB[22]), .cin(carry[21]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[22]), .cout(carry[22]) );
    ALU1bit alu23( .a(dataA[23]), .b(dataB[23]), .cin(carry[22]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[23]), .cout(carry[23]) );
    ALU1bit alu24( .a(dataA[24]), .b(dataB[24]), .cin(carry[23]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[24]), .cout(carry[24]) );
    ALU1bit alu25( .a(dataA[25]), .b(dataB[25]), .cin(carry[24]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[25]), .cout(carry[25]) );
    ALU1bit alu26( .a(dataA[26]), .b(dataB[26]), .cin(carry[25]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[26]), .cout(carry[26]) );
    ALU1bit alu27( .a(dataA[27]), .b(dataB[27]), .cin(carry[26]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[27]), .cout(carry[27]) );
    ALU1bit alu28( .a(dataA[28]), .b(dataB[28]), .cin(carry[27]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[28]), .cout(carry[28]) );
    ALU1bit alu29( .a(dataA[29]), .b(dataB[29]), .cin(carry[28]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[29]), .cout(carry[29]) );
    ALU1bit alu30( .a(dataA[30]), .b(dataB[30]), .cin(carry[29]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[30]), .cout(carry[30]) );
    ALU1bit alu31( .a(dataA[31]), .b(dataB[31]), .cin(carry[30]),  .binvert(binvert), .op(op), .less(1'b0), .result(res[31]), .cout(carry[31]) );

    assign set = res[31];
    assign dataOut = reset ? 32'b0 : res;
endmodule
