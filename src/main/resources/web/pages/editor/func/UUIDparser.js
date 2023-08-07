const DIGITS = [
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, -1, -1,
    -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
    25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, -1, -1, -1, -1, -1, -1, 10, 11, 12,
    13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32,
    33, 34, 35, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
];

function calcDigit(ch, radix) {
    value = DIGITS[ch];
    return (value >= 0 && value < radix && radix >= 2
            && radix <= 36) ? value : -1;
}

function parseLong(str, begin, end, radix) {
    negative = false;
    i = begin;

    if (i < end) {
        firstChar = str[i];
        if (firstChar < '0') {
            if (firstChar == '-') {
                negative = true;
            }
            i++;
        }
        result = 0;
        while (i < end) {
            digit = calcDigit(str.codePointAt(i), radix);
            result *= radix;
            i++;
            result -= digit;
        }
        return (negative) ? result : -result;
    }

    return 0;
}

function calcAllBytes(uuid) {
    let val = 0x800000000000;
    strArr = uuid.split("-");
    for (let i = 0; i < strArr.length; i++) {
        const s = strArr[i];
        val += parseLong(s, 0, s.length, 16);
    }
    return val;
}

function bytesToCmdCode(uuidBytes) {
    let result = "";

    const str = uuidBytes.toString(16);
    for (let i = 0; i < str.length - 1; i += 2) {
        const l1 = parseLong(str[i], 0, 1, 16);
        const l2 = parseLong(str[i + 1], 0, 1, 16);

        result += String.fromCharCode((l1 + l2 + 48));
    }

    return result;
}