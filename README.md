# Weaver

Weaver is a small extensible Data Structure templating library written in Clojure(Script) and targetting both the JVM and Lumo.

## Warning

This Library is still in VERY early stages of development and is highly opinionated to my personal use case.
I will do my best to review and accept any PRs, but I will only develop features that are relevant to my use cases for now.

There are some interop and utility functions included. Do not depend on any of these. They are purely for convenience and use inside the library. The API may change or functions may be removed at my discretion.

## Overview

Weaver uses `clojure.walk/postwalk` to transform and inject data into an EDN template.

The main pattern matching is on maps that have a `:weaver.processor/id` key, but there is a `pre-process-node` multimethod that provides two types of syntax sugar -namespaced keyword, and vector (a vector with a namespaced keyword as the first element).

## Details

The main functionality is `process` and associated functions which are backed by the `process-node` multimethod.
There are some processors provided, which are discussed below, but since `process-node` is a multimethod, it can be extended however you wish.


### End to end process (Rough and poorly worded)
  1. Call `process` with a raw context and a template
  2. Context is processed, and then the template is postwalked with `process-node`
  3. `process-node` dispatches on type.
      1. If it is a namespaced keyword (without a corresponding method in process-node) or a vector beginning with a namespaced keyword it goes to `:pre-process`:
          1. Call `pre-process-node` on it, returning a template in map form if there is a method matching `[:keyword "<kw-ns>"]` if it was a keyword, and `[:vector "<kw-ns>"]` and proceeding conditionally:
          2. If the node emerges unchanged (as determined by `=`), it falls through and the node is unchanged by `process-node`
          3. If the node is changed, the new value is inserted and is itself processed, potentially returning to `pre-process-node` again. (Note: the node is not walked into, it is just passed to process-node directly)
      2. If it is a map with `:weaver.processor/id` it is passed to the `process-node` method corresponding to the processor id which is expected to return the desired value. If no method is found, an error is thrown.
      3. Otherwise the node is left unchanged.
 
## Gotchas

  Vector syntax must not collide with keyword syntax, otherwise the leading keyword will always be transformed first

## Processors

### Config

Processors in the  `config` namespace are used to access the `:config` map within the processing context.

`config` has the following processors:

#### :config/get-in

Requires `:config` to be present in the context

Access the value at a path in `:config`

e.g.

```clojure
{:weaver.processor/id :config/get-in
 :path                [:foo :bar]
 :default             "<default-value>"}
```

This accesses the `:config` in the context, and returns the value at `:path`.
Returns `:default` if no value is found.

The following syntaxes are provided:

```clojure
[:config/get-in [:foo :bar] "default"]
;; =>
{:weaver.processor/id :config/get-in
 :path                [:foo :bar]
 :default             "default"}
 
;; For top level keys there is also: 

[:config/get :foo "default"]
;; =>
{:weaver.processor/id :config/get-in
 :path                [:foo]
 :default             "default"}
```

#### :config/get-in!

Requires `:config` to be present in the context

Access the value at a path in `:config`

e.g.

```clojure
{:weaver.processor/id :config/get-in!
 :path                [:foo :bar]}
```

This accesses the `:config` in the context, and returns the value at `:path`. 
Errors if a value is not found.

The following syntaxes are provided:

```clojure
[:config/get-in! [:foo :bar]]
;; =>
{:weaver.processor/id :config/get-in!
 :path                [:foo :bar]}
 
;; For top level keys there is also: 

[:config/get! :foo]
;; =>
{:weaver.processor/id :config/get-in!
 :path                [:foo]}
 
;; And the keyword syntax (which excludes :get, :get-in, :get!, and :get-in!)

:config/foo
;; =>
{:weaver.processor/id :config/get-in!
 :path                [:foo]}
```

### Env

Processors in the `env` namespace are used to access the shell environment.

`env` has the following processors:

#### :env/get


Access the specified environment variable

e.g.

```clojure
{:weaver.processor/id :env/get
 :name                "HOME"
 :default             "/home/foo"}
```
The following syntaxes are provided:

```clojure
[:env/get "HOME" "/home/foo"]
;; =>
{:weaver.processor/id :env/get
 :name                "HOME"
 :default             "/home/foo"}
```

#### :env/get!

Access the specified environment variable, error if not found

e.g.

```clojure
{:weaver.processor/id :env/get!
 :name                "HOME"}
```
The following syntaxes are provided:

```clojure
[:env/get! "HOME"]
;; =>
{:weaver.processor/id :env/get!
 :name                "HOME"}
 
:env/HOME
;;=>
{:weaver.processor/id :env/get!
 :name                "HOME"}
```
### Fn

TODO: Replicate above format of documentation

`:fn/<some-fn-name>`

Looks up the function in either the default function lookup or a `:function-lookup` passed in via the context map.
Note that the lookup uses the namespaced keywords, so make sure that any provided `:function-lookup` is a namespaced map (i.e. `#:fn{:first first}`)
Defaults currently contains: str, =, <, >, <=, >= 


### Format

TODO: Replicate above format of documentation

`:format/cl-format`

Takes `:string` and `:args`. Calls `clojure.pprint/cl-format` to process `:string` as as format-string with `:args` as args.

### Git

TODO: Replicate above format of documentation

`:git/short-hash`

Returns the short hash of HEAD

### Time

TODO: Replicate above format of documentation

`:time/from-long`

Takes `:long`. returns either a clj-time object or a cljs-time object depending on the runtime. Intended for use by other processors.

`:time/format`

Takes `:time` and optional `:time-zone` and `:format-string`. Formats time according to time-zone and format-string. Defaults to "UTC" and "yyyy-MM-ddTHH:mm:ss.SSSZZZ".
Template wide defaults can be specified in the context at `:time-zone` and `:format-string`.

### Weaver

TODO: Replicate above format of documentation

Generally applicable processors

`:weaver/drop-nils`

```
{:weaver.processor/id :weaver/drop-nils
 :coll [list of vals or processors]}
```

```
[:weaver/drop-nils nilable-processor1 nilable-processor2 nilable-processor3]
```

`:weaver/if`

```
{:weaver.processor/id :weaver/if
 :pred [some processor]
 :if [some processor for truthy case]
 :else [some processor for falsey case]}
```

```
[:weaver/if :pred :if :else]
```
`:weaver/when`

```
{:weaver.processor/id :weaver/when
 :pred [some processor]
 :val [some processor if truthy]}
```

```
[:weaver/when :pred :val]
```

`:weaver/cond`

```
{:weaver.processor/id :weaver/cond
 :clauses [pred1 processor1 pred2 processor2 ...]}
```

```
[:weaver/cond pred1 processor1 :else processor-else]
```


### Context or ctx (ADVANCED)

Processors in the `ctx` namespace are intended as processors of last resort to stand in for more purpose-built extensions.

`ctx` has the following processors:

#### :ctx/get-in-resource

Similar to the config accessor, but for arbitrary context resources.

Requires the accessed resource (i.e. the value at `:resource-id`) to be present in the context. 


e.g.

```clojure
{:weaver.processor/id :ctx/get-in-resource
 :resource-id         :something
 :path                [:foo :bar]
 :default             "default"}
```

This accesses the path `[:foo :bar]` in the `:something` resource, returning `"default"` if it isn't found.

#### :ctx/get-in-resource!

Similar to the config accessor, but for arbitrary context resources.

Requires the accessed resource (i.e. the value at `:resource-id`) to be present in the context. 

e.g.

```clojure
{:weaver.processor/id :ctx/get-in-resource!
 :resource-id         :something
 :path                [:foo :bar]}
```

This accesses the path `[:foo :bar]` in the `:something` resource, erroring if it isn't found.

The following syntaxes are provided:

```clojure
[:ctx.get-in/something [:foo :bar]]
;; =>
{:weaver.processor/id :ctx/get-in-resource!
 :resource-id         :something
 :path                [:foo :bar]}
```

#### :ctx/call

Evaluates a function in the context with the provided argument list.

Requires the accessed function (i.e. the value at `:function-id`) to be present in the context. 

e.g.

```clojure
{:weaver.processor/id :ctx/call
 :function-id         :plus
 :args                [1 2]}
```

This applies the function at `:plus` (presumably `+`) on `[1 2]` (presumably evaluating to `3`)

The following syntaxes are provided:

```clojure
[:ctx.call/plus [1 2]]
;; =>
{:weaver.processor/id :ctx/call
 :function-id         :plus
 :args                [1 2]}
```

### Development mode

To start the Figwheel compiler, navigate to the project folder and run the following command in the terminal:

```
lein figwheel
```

Test from the repl

### Building for production

TODO

NOTE: npm repo will be weaver-cljs


### Libraries that use Weaver
  - [Albatross](https://github.com/SVMBrown/albatross#albatross)
