"""
ECSE 316: Assignment 2
Fast Fourier Transform and Applications

April 5th, 2021

Charles Bourbeau (260868653)
Felix Simard (260865674)
Group 40
"""

import sys
from helper import *


def parseCommandLineArgs():
    # default parameters
    mode = 1
    image = 'moonlanding.png'

    args_lst = sys.argv
    num_args = len(args_lst)

    if num_args == 5:
        mode = args_lst[2]
        image = args_lst[4]
    elif num_args == 3:
        # determine if got -m or -i
        if "-m" in args_lst:
            mode = args_lst[2]
        if "-i" in args_lst:
            image = args_lst[2]

    args = {
        'mode': int(mode),
        'image': str(image)
    }
    return args


def main():
    args = parseCommandLineArgs()
    print(args)


# MAIN
if __name__ == '__main__':
    main()
