/*******************************************************************************
 *
 *    Copyright 2020 Adobe. All rights reserved.
 *    This file is licensed to you under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License. You may obtain a copy
 *    of the License at http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software distributed under
 *    the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 *    OF ANY KIND, either express or implied. See the License for the specific language
 *    governing permissions and limitations under the License.
 *
 ******************************************************************************/

const path = require('path');

const BUILD_DIR = path.join(__dirname, 'dist');
const CLIENTLIB_DIR = path.join(
  __dirname,
  '..',
  'ui.apps',
  'src',
  'main',
  'content',
  'jcr_root',
  'apps',
  'centralthai',
  'clientlibs'
);

const libsBaseConfig = {
  allowProxy: true,
  serializationFormat: 'xml',
  cssProcessor: ['default:none', 'min:none'],
  jsProcessor: ['default:none', 'min:none']
};

// Config for `aem-clientlib-generator`
module.exports = {
    context: BUILD_DIR,
    clientLibRoot: CLIENTLIB_DIR,
    libs: [
        {
            ...libsBaseConfig,
            name: 'clientlib-site',
            categories: ['centralthai.site'],
            dependencies: ['centralthai.dependencies', 'aem-core-cif-react-components'],
            assets: {
                // Copy entrypoint scripts and stylesheets into the respective ClientLib
                // directories
                js: {
                    cwd: 'clientlib-site',
                    files: ['**/*.js'],
                    flatten: false
                },
                css: {
                    cwd: 'clientlib-site',
                    files: ['**/*.css'],
                    flatten: false
                },

                // Copy all other files into the `resources` ClientLib directory
                resources: {
                    cwd: 'clientlib-site',
                    files: ['**/*.*'],
                    flatten: false,
                    ignore: ['**/*.js', '**/*.css']
                }
            }
        },
        {
            ...libsBaseConfig,
            name: 'clientlib-site-coral',
            categories: ['centralthai.site.coral'],
            dependencies: ['centralthai.dependencies', 'aem-core-cif-react-components'],
            assets: {
                // Copy entrypoint scripts and stylesheets into the respective ClientLib
                // directories
                js: {
                    cwd: 'clientlib-site-coral',
                    files: ['**/*.js'],
                    flatten: false
                },
                css: {
                    cwd: 'clientlib-site-coral',
                    files: ['**/*.css'],
                    flatten: false
                },

                // Copy all other files into the `resources` ClientLib directory
                resources: {
                    cwd: 'clientlib-site-coral',
                    files: ['**/*.*'],
                    flatten: false,
                    ignore: ['**/*.js', '**/*.css']
                }
            }
        }
    ]
};
