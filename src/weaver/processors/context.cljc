(ns weaver.processors.context
  (:require
   [weaver.interop :as x]
   [weaver.processors.multi :refer [pre-process-node process-node context-required-for-processor]]))

(defmethod pre-process-node [:vector "ctx.get-in"] [[kw path default :as v]]
  (if (= 3 (count v))
    {:weaver.processor/id :ctx/get-in-resource
     :resource-id (keyword (name kw))
     :path path
     :default default}
    {:weaver.processor/id :ctx/get-in-resource!
     :resource-id (keyword (name kw))
     :path path}))

(defmethod context-required-for-processor :ctx/get-in-resource [{:keys [resource-id]}]
  #{resource-id})

(defmethod process-node :ctx/get-in-resource [ctx {:keys [resource-id path default]}]
  (if-some [resource (get ctx resource-id)]
    (get-in resource path default)
    (x/warn-and-exit (str "Resource: " resource-id " not found in context! Please provide " resource-id " and try again."))))

(defmethod context-required-for-processor :ctx/get-in-resource! [{:keys [resource-id]}]
  #{resource-id})

(defmethod process-node :ctx/get-in-resource! [ctx {:keys [resource-id path] :as node}]
  (if-some [result (process-node ctx (assoc node :weaver.processor/id :ctx/get-in-resource))]
    result
    (x/warn-and-exit
     (str "Error in node: " node)
     (str "Value not found in provided resource: " resource-id " at path: " path "."))))

(defmethod pre-process-node [:vector "ctx.call"] [[kw & args]]
  {:weaver.processor/id :ctx/call
   :function-id (keyword (name kw))
   :args args})

(defmethod pre-process-node [:keyword "ctx.call"] [kw]
  (pre-process-node [kw]))

(defmethod context-required-for-processor :ctx/call [{:keys [function-id]}]
  #{function-id})

(defmethod process-node :ctx/call [ctx {:keys [function-id args]}]
  (if-some [function (get ctx function-id)]
    (if (ifn? function)
      (apply function args)
      (x/warn-and-exit (str  function-id " in context cannot be used as a function. Please provide a function and try again.")))
    (x/warn-and-exit (str "Function: " function-id " not found in context! Please provide " function-id " and try again."))))
