# your code goes here

BASE62 = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ'

def decode(string, alphabet=BASE62):
    """
    Decode a Base X encoded string into the number

    Arguments:
    - `string`: The encoded string
    - `alphabet`: The alphabet to use for decoding

    """
    base = len(alphabet)
    strlen = len(string)
    num = 0

    idx = 0
    for char in string:
        power = (strlen - (idx + 1))
        num += alphabet.index(char) * (base ** power)
        idx += 1

    return str(num)
    
#if __name__ == "main":
num = input()
try :
	print(decode(str(num), BASE62))
except ValueError:
	print("please enter a valid input :)")
		