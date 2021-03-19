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
import numpy as np
import numpy.fft
from math import pi


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


def _dft(arr, k):
    def gen():
        N = len(arr)
        for n in range(N):
            xn = arr[n]
            coef = np.exp(-1j * 2 * pi * k * n / N)

            yield xn * coef

    return sum(gen())


def DFT(arr):
    res = np.zeros(arr.shape, dtype='complex_')
    N = len(arr)
    for k in range(N):
        res[k] = _dft(arr, k)

    return res


def DFT_INVERSE(arr):
    res = np.zeros(arr.shape, dtype='complex_')
    N = len(arr)
    for k in range(N):
        def gen():
            N = len(arr)
            for n in range(N):
                xn = arr[n]
                coef = np.exp(1j * 2 * pi * k * n / N)

                yield xn * coef

        res[k] = sum(gen())

    res = 1/N * res
    return res


def FFT(arr: np.ndarray, threshold):
    assert type(arr) is np.ndarray

    def _fft(_arr, k):
        if len(_arr) < threshold:
            #print(_dft(_arr, k))
            return _dft(_arr, k)
        else:
            N = len(_arr)
            ns = set(range(N))
            evens = set(i for i in ns if i % 2 == 0)
            odds = ns - evens
            evens, odds = np.array(list(evens)), np.array(list(odds))

            Xeven = _fft(_arr[evens], k)
            Xodd = _fft(_arr[odds], k)

            return Xeven + np.exp(-1j*2*pi*k/N) * Xodd

    res = np.array([])
    for k in range(len(arr)):
        res = np.append(res, _fft(arr, k))

    return res


def DFT_2D(matrix: np.ndarray):
    assert type(matrix) is np.ndarray

    N, M = matrix.shape

    res = np.zeros(matrix.shape, dtype='complex_')
    for l in range(N):
        for k in range(M):

            def inner_gen(row):
                for m in range(M):
                    xn = row[m]
                    coef = np.exp(-1j * 2 * pi * k * m / M)

                    yield xn * coef

            def outer_gen():
                for n in range(N):
                    row = matrix[n, :]

                    xn = sum(inner_gen(row))
                    coef = np.exp(-1j * 2 * pi * l * n / N)

                    yield xn * coef

            res[l, k] = sum(outer_gen())

    return res


def main():
    args = parseCommandLineArgs()
    print(args)


# MAIN
if __name__ == '__main__':
    main()

    array = np.array([1,2,3,2,1])
    print()
    # print('ours: ', FFT(array, 2))
    # print(DFT(array))
    # print('np: ', numpy.fft.fft(array))

    print(DFT_INVERSE(array))
    print(np.fft.ifft(array))


    a = np.array([[1,2],[3,4],[5,6]])
    print(np.fft.fft2(a))
    print(DFT_2D(a))
