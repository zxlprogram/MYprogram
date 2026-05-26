`timescale 1ns/1ns
module Divider( clk, dataA, dataB, Signal, dataOut, reset );
input clk ;
input reset ;
input [31:0] dataA ;
input [31:0] dataB ;
input [5:0] Signal ;
output [63:0] dataOut ;

//   Signal ( 6-bits)?
//   DIVU  : 27

reg [63:0] temp ;
parameter DIVU = 6'b011011;
parameter OUT = 6'b111111;
/*
定義各種訊號
*/
/*
=====================================================
下面為模擬範例，程式撰寫請遵照老師上課說明的方法來寫
=====================================================
*/
always@( posedge clk or reset )
begin
        if ( reset )
        begin
                temp = 32'b0 ;
        end
/*
reset訊號 如果是reset就做歸0
*/
        else
        begin
		case ( Signal )
  		DIVU:
		begin

		end
  		OUT:
		begin
			 temp[63:32] = dataA / dataB ;
			 temp[31:0]= dataA % dataB ;
			#330 ;
		end
		endcase
        end
/*
除法運算
OUT的部分是要等control給你指令你才能夠把答案輸出到HILO暫存器
*/

end
assign dataOut = temp ;

/*
=====================================================
上面為模擬範例，程式撰寫請遵照老師上課說明的方法來寫
=====================================================
*/
endmodule