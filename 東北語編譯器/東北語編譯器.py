# -*- coding: utf-8 -*-
import re
import traceback
KEYWORDS = {
    '假的': 'False',
    '空空如也': 'None',
    '真的': 'True',
    '而且': 'and',
    '就像': 'as',
    '你瞅瞅這對不對': 'assert',
    '等一哈': 'async',
    '等它整完再說': 'await',
    '整完了': 'break',
    '弄個玩意兒': 'class',
    '接著整': 'continue',
    '整一個': 'def',
    '幹掉': 'del',
    '要不行咧就尋思': 'elif',
    '要不行咧': 'else',
    '整出岔子就整': 'except',
    '最後來整這些': 'finally',
    '墨跡': 'for',
    '從': 'from',
    '咱們的': 'global',
    '尋思': 'if',
    '整來': 'import',
    '裡頭有': 'in',
    '是': '==',
    '匆忙寫個': 'lambda',
    '別地兒的': 'nonlocal',
    '否則': 'not',
    '或是': 'or',
    '咱不興這個': 'pass',
    '你給我抬出去': 'raise',
    '滾犢子吧': 'return',
    '咱試試': 'try',
    '一直整到': 'while',
    '帶上': 'with',
    '喊它出來點東西': 'yield',
    '嘀咕':'print',
    '你說句話':'input()',
    '有毒吧':'int',
    '比這玩意大-->':'>',
    '比這玩意小-->':'<',
    '也許比這玩意大':'>=',
    '也許比這玩意小':'<=',
    '裝':'=',
    '我抽你一個比兜':'True',
    '加':'+',
    '減':'-',
    '乘':'*',
    '除':'/',
    '套個娃':'append',
    '這坨玩意':'range',
}
def translate(tuhai_code):
    code = tuhai_code
    for k, v in KEYWORDS.items():
        code = re.sub(rf'\b{k}\b', v, code)
    return code

if __name__ == '__main__':
    try:
        lines = []
        while True:
            line = input()
            if line.strip() == '就這樣了老鐵':
                break
            lines.append(line)

        code = '\n'.join(lines)
        exec(translate(code),globals())
        input()
    except Exception as e:
        print("老弟，你整出岔子了:",e)
        traceback.print_exc()
        input()