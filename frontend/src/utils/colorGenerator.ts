import {red, sky, amber, green, blue, cyan, fuchsia, indigo,lime, emerald, orange, pink, rose, teal, zinc, yellow} from 'tailwindcss/colors';

export const colors = [
    red,
    sky,
    amber,
    blue,
    cyan,
    emerald,
    fuchsia,
    green,
    indigo,
    lime,
    orange,
    pink,
    rose,
    teal,
    yellow,
    zinc
] as const;

export function colorGenerator() {
    return colors[Math.floor(Math.random() * colors.length)];
}