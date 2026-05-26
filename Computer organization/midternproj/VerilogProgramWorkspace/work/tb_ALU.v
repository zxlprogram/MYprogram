`timescale 1ns/ 1ns

module tb_ALU();
    reg clk, rst;
    reg[5:0] ctrl;
    reg[31:0] inputA, inputB, ans;
    wire[31:0] out;
    
    integer fp_r, fp_r_ans;
    integer status;
    initial begin
        clk = 1'b1;
        forever #5 clk = ~clk;
    end

    initial begin
        rst = 1'b1;
        ctrl = 6'b0;
        inputA = 32'b0;
        inputB = 32'b0;
        
        #10;
        rst = 1'b0;
        fp_r     = $fopen("input.txt", "r");
        fp_r_ans = $fopen("ans.txt", "r");
        if (fp_r == 0 || fp_r_ans == 0) begin
            $display("Error: Could not open input.txt or ans.txt");
            $finish;
        end

        $display("--- Simulation Start ---\n");
        status = $fscanf(fp_r_ans, "%d", ans);

        while (!$feof(fp_r_ans)) begin
            status = $fscanf(fp_r, "%d%d%d", ctrl, inputA, inputB);
            
            $write("%t: Input: ", $time);
            case(ctrl)
                6'd36: $write("AND(%d)   ", ctrl);
                6'd37: $write("OR(%d)    ", ctrl);
                6'd32: $write("ADD(%d)   ", ctrl);
                6'd34: $write("SUB(%d)   ", ctrl);
                6'd42: $write("SLT(%d)   ", ctrl);
                6'd2 : $write("SRL(%d)   ", ctrl);
                6'd25: $write("MULTU(%d) ", ctrl);
                default: $write("OP(%d)    ", ctrl);
            endcase
            $display("A:%d B:%d", inputA, inputB);

            if (ctrl == 6'd25) begin
                #330;
                $display("%t: Multiplier Finished", $time);
                
                #10;
                ctrl = 6'd16; 
                #10;
                if (ans == out)
                    $display("    Correct (Hi): Your: %d, Correct: %d", out, ans);
                else
                    $display("    WRONG (Hi):   Your: %d, Correct: %d", out, ans);
                ctrl = 6'd18;
                status = $fscanf(fp_r_ans, "%d", ans);
                #10;
                if (ans == out)
                    $display("    Correct (Lo): Your: %d, Correct: %d\n", out, ans);
                else
                    $display("    WRONG (Lo):   Your: %d, Correct: %d\n", out, ans);
            end
            else begin
                #10;
                if (ans == out)
                    $display("    Correct: Your: %d, Correct: %d\n", out, ans);
                else
                    $display("    WRONG:   Your: %d, Correct: %d\n", out, ans);
            end
            status = $fscanf(fp_r_ans, "%d", ans);
        end

        $fclose(fp_r);
        $fclose(fp_r_ans);
        $display("--- Simulation End ---");
        $stop;
    end
    TotalALU alu (
        .clk(clk), 
        .reset(rst), 
        .dataA(inputA),
        .dataB(inputB), 
        .Signal(ctrl), 
        .Output(out)
    );

endmodule