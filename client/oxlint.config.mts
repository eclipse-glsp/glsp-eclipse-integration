/********************************************************************************
 * Copyright (c) 2026 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
import glspConfig from '@eclipse-glsp/oxlint-config';
import { defineConfig } from 'oxlint';

// Relative index and src imports restricted by the shared @eclipse-glsp/oxlint-config.
// Must be included in every `no-restricted-imports` override since an override replaces the entire rule value.
const restrictedBaseImports = ['..', '../index', '../..', '../../index', 'src'];

export default defineConfig({
    extends: [glspConfig],
    options: {
        // `typeAware` enables the type-aware rules of the shared config. `typeCheck` additionally
        // reports the TypeScript compiler diagnostics of the same program, so `pnpm lint` is a
        // complete static check without a prior build.
        typeAware: true,
        typeCheck: true
    },
    // Ignore JS/MJS/CJS config/build files, generated output and local git worktrees.
    ignorePatterns: [
        '**/{node_modules,lib,dist}',
        '**/*.d.ts',
        '**/*.map',
        '**/*.js',
        '**/*.mjs',
        '**/*.cjs',
        // Standalone maintenance scripts, run directly by Node and not part of any tsconfig
        'scripts/',
        '.worktrees/',
        // Bundled webapp, copied into the Eclipse workflow example plugin
        'examples/*/app/'
    ],
    overrides: [
        // The sprotty defaults are customized and re-exported by GLSP.
        {
            files: ['packages/**/*.{ts,tsx}', 'examples/**/*.{ts,tsx}'],
            rules: {
                'no-restricted-imports': [
                    'warn',
                    ...restrictedBaseImports,
                    {
                        name: 'sprotty',
                        message:
                            "The sprotty default exports are customized and reexported by GLSP. Please use '@eclipse-glsp/client' instead"
                    },
                    {
                        name: 'sprotty-protocol',
                        message:
                            "The sprotty-protocol default exports are customized and reexported by GLSP. Please use '@eclipse-glsp/protocol' or '@eclipse-glsp/client' instead"
                    }
                ]
            }
        }
    ]
});
