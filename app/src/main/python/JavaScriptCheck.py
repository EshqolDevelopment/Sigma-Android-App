import js2py
from ast import literal_eval


def java_script(parameters, function, mult):
    js = function
    result = js2py.eval_js(js)

    if mult:
        return result(*parameters)

    elif parameters == "<>":
        return result()

    else:
        return result(parameters)


def check_js(function, func_name, inp_dict, output_dict, multi_argument):
    try:
        multi_argument = multi_argument == "true"
        inp_dict = literal_eval(inp_dict)
        output_dict = literal_eval(output_dict)

        input_arr = inp_dict[func_name]
        output_arr = output_dict[func_name]

        if len(input_arr) == 0:
            t1 = java_script([], function, False)
            t2 = output_arr
            if str(t1) == str(t2):
                return True
            return "Incorrect Answer"

        for i in range(len(input_arr)):
            if type(output_arr[i]) == list or type(output_arr) == dict:
                if str(java_script(input_arr[i], function, multi_argument)) != str(output_arr[i]):
                    return "Incorrect Answer"

            elif type(output_arr[i]) == tuple:
                if str(java_script(input_arr[i], function, multi_argument)).replace("(", "[").replace(")", "]") != str(output_arr[i]).replace("(", "[").replace(")", "]"):
                    return "Incorrect Answer"

            else:
                if java_script(input_arr[i], function, multi_argument) != output_arr[i]:
                    return "Incorrect Answer"
        return True

    except Exception as e:
        return e
