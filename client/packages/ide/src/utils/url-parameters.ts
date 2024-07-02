/*
 * Copyright (c) 2017-2020 TypeFox and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 */

export function getParameters(): { [key: string]: string } {
    let search = window.location.search.substring(1);
    const result: { [key: string]: string } = {};
    while (search.length > 0) {
        const nextParamIndex = search.indexOf('&');
        let param: string;
        if (nextParamIndex < 0) {
            param = search;
            search = '';
        } else {
            param = search.substring(0, nextParamIndex);
            search = search.substring(nextParamIndex + 1);
        }
        const valueIndex = param.indexOf('=');
        if (valueIndex > 0 && valueIndex < param.length - 1) {
            result[param.substring(0, valueIndex)] = decodeURIComponent(param.substring(valueIndex + 1));
        }
    }
    return result;
}
