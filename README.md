# shadowroot-search

[![Build Status](https://travis-ci.org/densudas/shadowroot-search.svg?branch=main)](https://travis-ci.com/github/densudas/shadowroot-search "Travis CI")
[![codecov](https://codecov.io/gh/densudas/shadowroot-search/branch/main/graph/badge.svg)](https://codecov.io/gh/densudas/shadowroot-search)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

### Limitations:

- An element can't be found by Xpath at the first level of shadow-root element.

```html
<div>
  #shadow-root
    <button id="inside-shadow-root"></button>
  <button id="outside-shadow-root"></button>
</div>
```

Button with id="inside-shadow-root" inside shadow-root **can't** be found by (any) xpath:

* "//button[@id='inside-shadow-root']"

Button with id="inside-shadow-root" inside shadow-root **can't** be found by css:

* "div button#inside-shadow-root"
