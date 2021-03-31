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
from matplotlib.colors import LogNorm
import pandas as pd

DEBUG = False


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

    print("Computing FFT 2D...")

    for i, row in enumerate(matrix):
        if DEBUG:
            if i % 100 == 0:
                print(i)
        res1[i] = FFT(row, 2)

    res2 = np.zeros(matrix.T.shape, dtype='complex_')
    for i, col in enumerate(res1.T):
        if DEBUG:
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
        second_mode(filename)
    elif mode == 3:
        third_mode(filename)
    else:
        fourth_mode()

    return


def first_mode(img):
    print("Mode 1")
    with Image.open('./' + img + '') as im:
        # convert image file to matrix
        data = np.asarray(im)

        # initialize a matrix which has sizes proportional to powers of 2 for FFT to work
        data_padded = np.zeros((closestpow2(data.shape[0]), closestpow2(data.shape[1])))

        # copy over the matrix data to our padded matrix
        data_padded[:data.shape[0], :data.shape[1]] = data

        # Compute the 2D-FFT on the image
        fft_2d = FFT_2D(data_padded)
        # For plotting purposes, take the real values of the matrix to eliminate complex values
        fft_2d_img = abs(fft_2d)

        # Plotting
        print("Plotting...")
        fig, ax = plt.subplots(nrows=1, ncols=2, figsize=(10, 5))
        ax[0].imshow(data, norm=LogNorm(), cmap=plt.cm.Greys,
                     interpolation='none')
        ax[1].imshow(fft_2d_img, norm=LogNorm(), cmap=plt.cm.Greys,
                     interpolation='none')
        plt.show()

        return


def remove_high_frequencies(matrix, remove=0.1):
    if remove > 0:
        # Define % of highest values to set to 0
        denoise_percent = remove;
        # Total number of entries
        total_size = matrix.size
        # Flatten our matrix
        matrix_flat = matrix.flatten()
        # Get indices of largest N values
        n = round(denoise_percent * total_size)
        indices = np.argpartition(matrix_flat, -n)[-n:]
        indices = indices[np.argsort(-matrix_flat[indices])]
        # Unravel indices
        high_frequencies = np.unravel_index(indices, matrix.shape)
        # Set high frequencies to 0
        matrix[high_frequencies] = 0

        # Print fraction of non-zero entries
        print("--------------")
        print("Non-zero entries:", total_size - len(indices))
        print("Fraction of non-zero entries:", round((total_size - len(indices)) / total_size, 2))
        print("--------------")

    else:
        # Print fraction of non-zero entries
        print("--------------")
        print("Non-zero entries:", matrix.size)
        print("Fraction of non-zero entries:", 1.0)
        print("--------------")

    return matrix


def second_mode(img):
    print("Mode 2")
    with Image.open('./' + img + '') as im:
        # convert image file to matrix
        data = np.asarray(im)

        # initialize a matrix which has sizes proportional to powers of 2 for FFT to work
        data_padded = np.zeros((closestpow2(data.shape[0]), closestpow2(data.shape[1])))

        # copy over the matrix data to our padded matrix
        data_padded[:data.shape[0], :data.shape[1]] = data

        # Compute the 2D-FFT on the image
        fft_2d = FFT_2D(data_padded)

        # Remove fraction of high frequencies
        fft_2d = remove_high_frequencies(matrix=fft_2d, remove=0.1)

        # Retain the real part of our
        fft_2d_img_inversed = np.fft.ifft2(fft_2d).real
        # fft_2d_img_inversed = DFT_2D_INVERSE(fft_2d_img).real

        # Plotting
        print("Plotting...")
        fig, ax = plt.subplots(nrows=1, ncols=2, figsize=(10, 5))
        ax[0].imshow(data, norm=LogNorm(), cmap=plt.cm.Greys,
                     interpolation='none')
        ax[1].imshow(fft_2d_img_inversed, norm=LogNorm(), cmap=plt.cm.Greys,
                     interpolation='none')
        plt.show()

        return


def third_mode(img):
    print("Mode 3")
    # Define our levels of compression to use
    compression_levels = [0, 0.2, 0.4, 0.65, 0.8, 0.95]
    with Image.open('./' + img + '') as im:
        # convert image file to matrix
        data = np.asarray(im)

        # initialize a matrix which has sizes proportional to powers of 2 for FFT to work
        data_padded = np.zeros((closestpow2(data.shape[0]), closestpow2(data.shape[1])))

        # copy over the matrix data to our padded matrix
        data_padded[:data.shape[0], :data.shape[1]] = data

        # Compute the 2D-FFT on the image
        fft_2d = FFT_2D(data_padded)

        # Plotting
        print("Plotting...")
        fig, ax = plt.subplots(nrows=2, ncols=3, figsize=(10, 5))

        print(ax[1])

        for i, lvl in enumerate(compression_levels):
            print("Compression %d percent" % (lvl * 100))
            fft_2d_compressed = remove_high_frequencies(matrix=fft_2d, remove=lvl)

            # Retain the real part of our
            fft_2d_compressed_inversed = np.fft.ifft2(fft_2d_compressed).real
            # fft_2d_compressed_inversed = DFT_2D_INVERSE(fft_2d_compressed).real

            # Save the matrix to a CSV
            csv_filename = "csv/compress_" + str(int(lvl * 100)) + ".csv"
            csv_file = open(csv_filename, "w")
            pd.DataFrame(fft_2d_compressed_inversed).to_csv(csv_file)

            if i in [0, 1, 2]:  # first row
                ax[0][i].imshow(fft_2d_compressed_inversed, norm=LogNorm(), cmap=plt.cm.Greys,
                                interpolation='none')
            else:  # second row
                ax[1][i - 3].imshow(fft_2d_compressed_inversed, norm=LogNorm(), cmap=plt.cm.Greys,
                                    interpolation='none')

        plt.show()

        return


def fourth_mode():
    print("Mode 4")
    sizes = [math.pow(2, 5), math.pow(2, 6), math.pow(2, 7), math.pow(2, 8), math.pow(2, 9), math.pow(2, 10)]
    runs = 4  # number of times to run our function for each input size
    stats = {}
    for i, size in enumerate(sizes):
        print("------------")
        print("Size:", size)

        # find the closest factors for the desired size of our matrix (as square as possible)
        r, c = closest_factors(size)
        # create a matrix of random integer values
        arr_rand = np.random.randint(0, 999999, size=(r, c), dtype='int')
        print(arr_rand)

        runtimes = np.zeros(runs)
        for i in range(runs):
            print("-",i)
            start = time.time()
            fft_2d = FFT_2D(arr_rand)
            end = time.time()
            elapsed = end - start
            np.append(runtimes, elapsed)
            print(runtimes)

        stats[str(size)] = {'var': np.var(runtimes), 'sd': np.std(runtimes)}

    print(stats)

    # Plot the statistics

    # ...


# MAIN
if __name__ == '__main__':
    main()
    # #
    # array = np.random.random(1024)
    # print()
    # print('ours: ', FFT(array, 5))
    # # # print(DFT(array))
    # print('np: ', numpy.fft.fft(array))
    #
    # print(np.allclose(FFT(array, 5), numpy.fft.fft(array)))
    # #
    # # print(DFT_INVERSE(array))
    # # print(np.fft.ifft(array))
    # #
    # #
    # # a = np.array([[1,2],[3,4],[5,6]])
    # # # print(np.fft.fft2(a))
    # # # print(DFT_2D(a))
    # # print()
    # # print(DFT_2D_INVERSE(a))
    # # print(np.fft.ifft2(a))
    #
    # # first_mode()
