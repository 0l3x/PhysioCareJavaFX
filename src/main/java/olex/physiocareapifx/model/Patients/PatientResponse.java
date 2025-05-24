package olex.physiocareapifx.model.Patients;

import olex.physiocareapifx.model.BaseResponse;
import olex.physiocareapifx.model.Physios.Physio;

public class PatientResponse extends BaseResponse {
        private Patient resultado;

        public Patient getPatient() {
            return resultado;
        }
}
