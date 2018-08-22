# Weaver

Weaver is a small extensible configuration pre-processing library written in Clojure(Script) and targetting both the JVM and Lumo.
The main functionality is `process` and associated functions which are backed by the `process-node` multimethod.
There are some processors provided, which are discussed below, but since `process-node` is a multimethod, it can be extended however you wish.

## Warning

This Library is still in VERY early stages of development and is highly opinionated to my personal use case.
I will do my best to review and accept any PRs, but I will only develop features that are relevant to my use cases for now.

There are some interop and utility functions included. Do not depend on any of these. They are purely for convenience and use inside the library. The API may change or functions may be removed at my discretion.

## Processors

### Config

`:config/<some-key-in-your-config>`

This transforms into the provided key in your config (passed in on the context map).
Will error and exit if the key isn't found. Equivalent to `[:config/get! <key>]`


`[:config/<get|get-in|get!|get-in!> <key-or-path-vec> <optional-default-value>]`

`get` and `get!` expect keys, `get-in` and `get-in!` expect path vectors.

`get` and `get-in` optionally take a default value and fall back to nil if one isn't provided, 
`get!` and `get-in!` will error if the value is absent in the config.

### Env

`:env/<some-case-sensitive-env-variable>`

This will look up an environment variable and error out if it isn't found.

`[:env/<get|get!> <case-sensitive-env-variable> <optional-default-value>]`

Same behaviour as in config, but accesses environment instead.

### Fn

`:fn/<some-fn-name>`

Looks up the function in either the default function lookup or a `:function-lookup` passed in via the context map.
Note that the lookup uses the namespaced keywords, so make sure that any provided `:function-lookup` is a namespaced map (i.e. `#:fn{:first first}`)

### Git

`:git/short-hash`

Returns the short hash of HEAD

### Development mode

To start the Figwheel compiler, navigate to the project folder and run the following command in the terminal:

```
lein figwheel
```

Test from the repl

### Building for production

TODO

NOTE: npm repo will be weaver-cljs
