package requests.skelethon.interfaces;

import models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);
    Object put(BaseModel model);
    Object get();
    Object delete(BaseModel model);
}
