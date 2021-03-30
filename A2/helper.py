"""
ECSE 316: Assignment 2
Fast Fourier Transform and Applications

April 5th, 2021

Charles Bourbeau (260868653)
Felix Simard (260865674)
Group 40
"""

import math
import numpy as np


def errorMsg(msg):
    print('\nERROR: {}.\n'.format(msg))


def closestpow2(k):
    return np.power(2, math.ceil(math.log(k, 2)))
