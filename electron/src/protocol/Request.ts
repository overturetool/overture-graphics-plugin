/**
 * Created by John on 31-05-2016.
 */
export class Request {
    static messageType: string = "REQUEST";
    static RUN_MODEL: string = "RunModel";
    static GET_MODEL_INFO: string = "GetModelInfo";
    static GET_CLASS_INFO: string = "GetClassInfo";
    static GET_FUNCTION_INFO: string = "GetFunctionInfo";
    static SET_ROOT_CLASS: string = "SetRootClass";
    static STOP_SERVER: string = "StopServer";
    request: string ;
    parameter: string ;
}