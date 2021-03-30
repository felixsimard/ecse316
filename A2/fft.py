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
from PIL import Image
import math
import timeit
import time
import matplotlib.pyplot as plt


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

    res = 1 / N * res
    return res


def dft(x):
    x = np.asarray(x, dtype=float)
    N = x.shape[0]
    n = np.arange(N)
    k = n.reshape((N, 1))
    M = np.exp(-2j * np.pi * k * n / N)
    return np.dot(M, x)


def FFT(arr: np.ndarray, threshold):
    assert type(arr) is np.ndarray

    def _fft(_arr, k):
        if len(_arr) < threshold:
            res = _dft(_arr, k)
            return res
        else:
            N = len(_arr)
            Xeven = _fft(_arr[::2], k)
            Xodd = _fft(_arr[1::2], k)

            exponens = np.exp(-1j * 2 * pi * np.arange(N) / N)

            return np.concatenate([Xeven + exponens[:int(N / 2)] * Xodd, Xeven + exponens[int(N / 2):] * Xodd])
            # return Xeven + np.exp(-1j*2*pi*k/N) * Xodd

    # res = np.array([])
    # for k in range(len(arr)):
    #     res = np.append(res, _fft(arr, k))

    return _fft(arr, threshold)
    # return res


# https://towardsdatascience.com/fast-fourier-transform-937926e591cb
def FFT_2D(matrix: np.ndarray):
    assert type(matrix) is np.ndarray

    res1 = np.zeros(matrix.shape, dtype='complex_')

    print(matrix.shape)
    for i, row in enumerate(matrix):
        if i % 100 == 0:
            print(i)
        res1[i] = FFT(row, 2)

    res2 = np.zeros(matrix.T.shape, dtype='complex_')
    for i, col in enumerate(res1.T):
        if i % 100 == 0:
            print(i)
        res2[i] = FFT(col, 2)

    return res2.T


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


def DFT_2D_INVERSE(matrix: np.ndarray):
    assert type(matrix) is np.ndarray

    N, M = matrix.shape

    res = np.zeros(matrix.shape, dtype='complex_')
    for l in range(N):
        for k in range(M):

            def inner_gen(row):
                for m in range(M):
                    xn = row[m]
                    coef = np.exp(1j * 2 * pi * k * m / M)

                    yield xn * coef

            def outer_gen():
                for n in range(N):
                    row = matrix[n, :]

                    xn = sum(inner_gen(row))
                    coef = np.exp(1j * 2 * pi * l * n / N)

                    yield xn * coef

            res[l, k] = sum(outer_gen())

    res = 1 / (N * M) * res
    return res


def main():
    args = parseCommandLineArgs()
    mode = args['mode']
    filename = args['image']

    if mode == 1:
        first_mode(filename)
    elif mode == 2:
        second_mode()
    elif mode == 3:
        third_mode()
    else:
        fourth_mode()

    return


def first_mode(img):
    print("Mode 1")
    with Image.open('./'+img+'') as im:
        data = np.asarray(im)

        a = np.zeros((closestpow2(data.shape[0]), closestpow2(data.shape[1])))

        a[:data.shape[0], :data.shape[1]] = data

        l = np.fft.fft2(a)
        print(l.shape)
        print(l)
        l2 = FFT_2D(a)
        print(l2.shape)
        print(l2)

        # # Compute the 2D-FFT on the image
        # fft_2d = FFT_2D(a)
        #
        # fig, ax = plt.subplots(nrows=1, ncols=2)
        # ax[0][0] = plt.imshow(fft_2d, interpolation='none')
        # plt.show()


def second_mode():
    print("Mode 2")


def third_mode():
    print("Mode 3")


def fourth_mode():
    print("Mode 4")


# MAIN
if __name__ == '__main__':
    # main()
    #
    array = np.random.random(1024)
    print()
    print('ours: ', FFT(array, 5))
    # # print(DFT(array))
    print('np: ', numpy.fft.fft(array))

    print(np.allclose(FFT(array, 5), numpy.fft.fft(array)))
    #
    # print(DFT_INVERSE(array))
    # print(np.fft.ifft(array))
    #
    #
    # a = np.array([[1,2],[3,4],[5,6]])
    # # print(np.fft.fft2(a))
    # # print(DFT_2D(a))
    # print()
    # print(DFT_2D_INVERSE(a))
    # print(np.fft.ifft2(a))

    # first_mode()
