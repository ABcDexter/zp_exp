# imports
import sys
import os
from json.decoder import JSONDecodeError

import requests
import json
################################


def do_post(api_name, spock):
    """
    takes API name in the vTech,
    also the auth header
    does the POST.
    """
    #url = os.getenv('ZP_SRV', 'https://api.zippe.in:8090/') + api_name
    url = 'http://127.0.0.1:9999/' + api_name

    headers = {'content-type': 'application/json', 'Accept-Charset': 'UTF-8'}
    # do the actual POST
    response = requests.post(url,spock, headers )
    # post(url ,data, json, kwargs)
    return response.content


# main function
if __name__ == "__main__":
    print(f"Arguments count: {len(sys.argv)}")
    api_name = ''
    dct = {}
    fileName = 'user' #{$1}-request.json'
    for i, arg in enumerate(sys.argv):
        print(f"Argument {i:>6}: {arg}")
        if i == 1:
            fileName += arg + '-request.json'
        elif i == 2:
            api_name += arg
        elif i==3:
            dct["auth"] = arg
        elif i%2==0:
            dct[arg] = sys.argv[i+1]
    print("api name : ", api_name, "\nand DATA dct : ", dct)
    #write the auth to a file
    with open(fileName, 'w') as f:
        f.write(json.dumps(dct))
    data = open(fileName)

    # call the POST method
    postResult = do_post(api_name, data)
    try:
        my_json = postResult.decode('utf8').replace("'", '"')
        data = json.loads(my_json)
        result = json.dumps(data, indent=4, sort_keys=True)
    except JSONDecodeError:
        print('Error in the API/POST')
        result = 'None'
    print(result)
    #for i,txt in enumerate(result):
    #   print (i, txt)



