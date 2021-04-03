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


def DFT(arr):
    assert type(arr) is np.ndarray

    if len(arr.shape) == 1:
        arr = np.array([arr])

    _, N = arr.shape

    ks = np.array([np.arange(N)])

    n = np.array([np.arange(N)])
    kn = np.matmul(ks.T, n)

    first_exponens = np.exp(-1j * 2 * pi * kn / N)

    a = np.matmul(arr, first_exponens.T)

    return a


def FFT(arr: np.ndarray, threshold):
    assert type(arr) is np.ndarray

    if len(arr) <= threshold:
        res = DFT(arr)
        return res
    else:
        N = len(arr)
        Xeven = FFT(arr[::2], threshold)
        Xodd = FFT(arr[1::2], threshold)

        exponens = np.exp(-1j * 2 * pi * np.arange(N) / N)

        return np.concatenate([Xeven + exponens[:int(N / 2)] * Xodd, Xeven + exponens[int(N / 2):] * Xodd], axis=1)


# https://towardsdatascience.com/fast-fourier-transform-937926e591cb
def FFT_2D(matrix: np.ndarray, threshold):
    assert type(matrix) is np.ndarray

    res1 = np.zeros(matrix.shape, dtype='complex_')

    print("Computing FFT 2D...")

    for i, row in enumerate(matrix):
        if DEBUG:
            if i % 100 == 0:
                print(i)
        res1[i] = FFT(row, threshold)

    res2 = np.zeros(matrix.T.shape, dtype='complex_')
    for i, col in enumerate(res1.T):
        if DEBUG:
            if i % 100 == 0:
                print(i)
        res2[i] = FFT(col, threshold)

    return res2.T


def DFT_2D(matrix: np.ndarray):
    assert type(matrix) is np.ndarray
    N, M = matrix.shape

    ks = np.array([np.arange(M)])
    ls = np.array([np.arange(N)])

    m = np.array([np.arange(M)])
    km = np.matmul(ks.T, m)

    n = np.array([np.arange(M)])
    ln = np.matmul(ls.T, n)

    first_exponens = np.exp(-1j * 2 * pi * km / M)

    second_exponens = np.exp(-1j * 2 * pi * ln / N)

    a = np.matmul(matrix.T, first_exponens.T).T

    c = np.matmul(a, second_exponens.T)

    return c


def DFT_2D_INVERSE(matrix: np.ndarray):
    assert type(matrix) is np.ndarray

    N, M = matrix.shape

    ks = np.array([np.arange(M)])
    ls = np.array([np.arange(N)])

    m = np.array([np.arange(M)])
    km = np.matmul(ks.T, m)

    n = np.array([np.arange(M)])
    ln = np.matmul(ls.T, n)

    first_exponens = np.exp(1j * 2 * pi * km / M)

    second_exponens = np.exp(1j * 2 * pi * ln / N)

    a = np.matmul(matrix.T, first_exponens.T).T

    c = np.matmul(a, second_exponens.T)

    return (1 / (N * M)) * c


def main():
    args = parseCommandLineArgs()
    mode = 3  # args['mode']
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
        fft_2d = FFT_2D(data_padded, 16)
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
        denoise_percent = remove
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
        print("Fraction of non-zero entries:", ((total_size - len(indices)) / total_size))
        print("--------------")

    else:
        # Print fraction of non-zero entries
        print("--------------")
        print("Non-zero entries:", matrix.size)
        print("Fraction of non-zero entries:", 1.0)
        print("--------------")

    return matrix


def remove_middle_freq(matrix: np.ndarray, percentile):
    total_size = matrix.size
    num_idx = percentile * total_size

    matrix_flat = matrix.flatten()
    argsorted = np.argsort(matrix_flat)

    middle = argsorted.size // 2

    middle_idx_flat = argsorted[-int(middle - num_idx / 2):int(middle + num_idx)]
    middle_idx = np.unravel_index(middle_idx_flat, matrix.shape)

    matrix[middle_idx] = 0

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
        fft_2d = np.fft.fft2(data_padded)  # FFT_2D(data_padded)

        # Remove fraction of high frequencies
        fft_2d = remove_high_frequencies(matrix=fft_2d, remove=0.0001)

        fft_2d_img_inversed = np.fft.ifft2(fft_2d).real
        fft_2d_img_inversed = fft_2d_img_inversed[:data.shape[0], :data.shape[1]]
        # fft_2d_img_inversed = DFT_2D_INVERSE(fft_2d_img).real

        # Plotting
        print("Plotting...")

        fig, ax = plt.subplots(nrows=1, ncols=2, figsize=(10, 5))
        ax[0].imshow(data,  # norm=LogNorm(),
                     cmap='gray',
                     interpolation='none')
        ax[1].imshow(fft_2d_img_inversed,  # norm=LogNorm(),
                     cmap='gray',
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
        fft_2d = FFT_2D(data_padded, 16)

        # Plotting
        print("Plotting...")
        fig, ax = plt.subplots(nrows=2, ncols=3, figsize=(10, 5))

        print(ax[1])

        for i, lvl in enumerate(compression_levels):
            print("Compression %d percent" % (lvl * 100))
            # fft_2d_compressed = remove_high_frequencies(matrix=fft_2d, remove=lvl)
            fft_2d_compressed = remove_middle_freq(matrix=fft_2d, percentile=lvl)

            # Retain the real part of our
            fft_2d_compressed_inversed = np.fft.ifft2(fft_2d_compressed).real
            fft_2d_compressed_inversed = fft_2d_compressed_inversed[:data.shape[0], :data.shape[1]]
            # fft_2d_compressed_inversed = DFT_2D_INVERSE(fft_2d_compressed).real

            # Save the matrix to a CSV
            csv_filename = "csv/compress_" + str(int(lvl * 100)) + ".csv"
            csv_file = open(csv_filename, "w")
            pd.DataFrame(fft_2d_compressed_inversed).to_csv(csv_file)

            if i in [0, 1, 2]:  # first row
                ax[0][i].imshow(fft_2d_compressed_inversed, # norm=LogNorm(),
                                cmap='gray',
                                interpolation='none')
            else:  # second row
                ax[1][i - 3].imshow(fft_2d_compressed_inversed, # norm=LogNorm(),
                                    cmap='gray',
                                    interpolation='none')

        plt.show()

        return


def fourth_mode():
    print("Mode 4")
    sizes = [math.pow(2, 5), math.pow(2, 6), math.pow(2, 7), math.pow(2, 8), math.pow(2, 9), math.pow(2, 10)]
    runs = 10  # number of times to run our function for each input size
    naive_stats = {}
    naive_std = {}
    mean_stats = {}
    var_stats = {}
    std_stats = {}
    for i, size in enumerate([int(s) for s in sizes]):
        print("------------")
        print("Size:", size)

        # find the closest factors for the desired size of our matrix (as square as possible)
        # r, c = closest_factors(size)
        # create a matrix of random integer values
        arr_rand = np.random.randint(0, 999999, size=(size, size), dtype='int')

        runtimes = np.zeros(runs)
        naive_runtimes = np.zeros(runs)
        for i in range(runs):
            start = time.time()
            fft_2d = np.fft.fft2(arr_rand)
            end = time.time()
            elapsed = end - start

            start2 = time.time()
            DFT_2D(arr_rand)
            end2 = time.time()
            elapsed2 = end2 - start2

            runtimes = np.append(runtimes, elapsed)
            naive_runtimes = np.append(naive_runtimes, elapsed2)

        naive_stats[size] = np.mean(naive_runtimes)
        naive_std[size] = np.std(naive_runtimes)
        mean_stats[size] = np.mean(runtimes)
        var_stats[size] = np.var(runtimes)
        std_stats[size] = np.std(runtimes)


    # Plot the statistics
    # df_mean = pd.DataFrame.from_dict(mean_stats, orient='index')
    # create mean dataframe
    columns = ['size', 'mean']
    data = np.column_stack([list(mean_stats.keys()), list(mean_stats.values())])
    df_mean = pd.DataFrame(data=data, columns=columns)
    print(df_mean)

    # create variance dataframe
    columns = ['size', 'var']
    data = np.column_stack([list(var_stats.keys()), list(var_stats.values())])
    df_var = pd.DataFrame(data=data, columns=columns)
    print(df_var)

    # create standard deviation dataframe
    columns = ['size', 'std']
    data = np.column_stack([list(std_stats.keys()), list(std_stats.values())])
    df_std = pd.DataFrame(data=data, columns=columns)
    print(df_std)

    # ...
    df_mean.plot(x='size', y='mean', kind='line')
    #df_var.plot(x='size', y='var', kind='line')
    #df_std.plot(x='size', y='std', kind='line')

    columns = ['size', 'fast_mean', 'fast_2std', 'naive_mean', 'naive_2std']
    data = np.column_stack([list(mean_stats.keys()), list(mean_stats.values()), [2*s for s in std_stats.values()],
                            list(naive_stats.values()), [2*s for s in naive_std.values()]])
    df_compare = pd.DataFrame(data=data, columns=columns)
    print(df_compare)

    #df_compare.plot(x='size', y=['fast_mean', 'naive_mean'], # yerr=['fast_2std', 'naive_2std'],
    #                kind='line')

    plt.errorbar(x=df_compare['size'], y=df_compare['fast_mean'], yerr=df_compare['fast_2std'])
    plt.errorbar(x=df_compare['size'], y=df_compare['naive_mean'], yerr=df_compare['naive_2std'])
    plt.show()

# MAIN
if __name__ == '__main__':
    main()
    # array = np.random.random((1024, 1024))
    # start1 = time.time()
    # ours = _DFT_2D(array)
    # t1 = time.time() - start1
    #
    # start2 = time.time()
    # theirs = np.fft.fft2(array)
    # t2 = time.time() - start2
    #
    # start3 = time.time()
    # past = FFT_2D(array, 16)
    # t3 = time.time() - start3
    #
    # print('t1:', t1)
    # print('t2:', t2)
    # print('t3:', t3)
    # print(np.allclose(ours, theirs, past))

    # array = np.random.random((1024, 1024))
    # ours = FFT_2D(array, 16)
    # theirs = np.array(np.fft.fft2(array))
    # print('ours', ours, ours.shape)
    # print('theirs', theirs, theirs.shape)
    # print(np.allclose(ours, theirs))

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
