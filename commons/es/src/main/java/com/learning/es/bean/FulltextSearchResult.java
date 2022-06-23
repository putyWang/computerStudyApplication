package com.learning.es.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 全文检索结果
 */
public class FulltextSearchResult {
    /**
     * 数据总条数
     */
    private long total = 0L;
    /**
     * 数据数组
     */
    private List<Result> data = new ArrayList<>();

    public FulltextSearchResult() {
    }

    public long getTotal() {
        return this.total;
    }

    public List<FulltextSearchResult.Result> getData() {
        return this.data;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public void setData(List<FulltextSearchResult.Result> data) {
        this.data = data;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof FulltextSearchResult)) {
            return false;
        } else {
            FulltextSearchResult other = (FulltextSearchResult)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (this.getTotal() != other.getTotal()) {
                return false;
            } else {
                Object this$data = this.getData();
                Object other$data = other.getData();
                if (this$data == null) {
                    if (other$data != null) {
                        return false;
                    }
                } else if (!this$data.equals(other$data)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof FulltextSearchResult;
    }

    public int hashCode() {
        int result = 1;
        long $total = this.getTotal();
        result = result * 59 + (int)($total >>> 32 ^ $total);
        Object $data = this.getData();
        result = result * 59 + ($data == null ? 43 : $data.hashCode());
        return result;
    }

    public String toString() {
        return "FulltextSearchResult(total=" + this.getTotal() + ", data=" + this.getData() + ")";
    }

    public static class Result {
        private String regNo;
        private String admNo;
        private String patientName;
        private String patientGender;
        private String admType;
        private String admDate;
        private String admDept;
        private String dishDate;
        private String dishDept;
        private String diagName;
        private String highLight;

        public Result() {
        }

        public String getRegNo() {
            return this.regNo;
        }

        public String getAdmNo() {
            return this.admNo;
        }

        public String getPatientName() {
            return this.patientName;
        }

        public String getPatientGender() {
            return this.patientGender;
        }

        public String getAdmType() {
            return this.admType;
        }

        public String getAdmDate() {
            return this.admDate;
        }

        public String getAdmDept() {
            return this.admDept;
        }

        public String getDishDate() {
            return this.dishDate;
        }

        public String getDishDept() {
            return this.dishDept;
        }

        public String getDiagName() {
            return this.diagName;
        }

        public String getHighLight() {
            return this.highLight;
        }

        public void setRegNo(String regNo) {
            this.regNo = regNo;
        }

        public void setAdmNo(String admNo) {
            this.admNo = admNo;
        }

        public void setPatientName(String patientName) {
            this.patientName = patientName;
        }

        public void setPatientGender(String patientGender) {
            this.patientGender = patientGender;
        }

        public void setAdmType(String admType) {
            this.admType = admType;
        }

        public void setAdmDate(String admDate) {
            this.admDate = admDate;
        }

        public void setAdmDept(String admDept) {
            this.admDept = admDept;
        }

        public void setDishDate(String dishDate) {
            this.dishDate = dishDate;
        }

        public void setDishDept(String dishDept) {
            this.dishDept = dishDept;
        }

        public void setDiagName(String diagName) {
            this.diagName = diagName;
        }

        public void setHighLight(String highLight) {
            this.highLight = highLight;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof FulltextSearchResult.Result)) {
                return false;
            } else {
                FulltextSearchResult.Result other = (FulltextSearchResult.Result)o;
                if (!other.canEqual(this)) {
                    return false;
                } else {
                    label143: {
                        Object this$regNo = this.getRegNo();
                        Object other$regNo = other.getRegNo();
                        if (this$regNo == null) {
                            if (other$regNo == null) {
                                break label143;
                            }
                        } else if (this$regNo.equals(other$regNo)) {
                            break label143;
                        }

                        return false;
                    }

                    Object this$admNo = this.getAdmNo();
                    Object other$admNo = other.getAdmNo();
                    if (this$admNo == null) {
                        if (other$admNo != null) {
                            return false;
                        }
                    } else if (!this$admNo.equals(other$admNo)) {
                        return false;
                    }

                    Object this$patientName = this.getPatientName();
                    Object other$patientName = other.getPatientName();
                    if (this$patientName == null) {
                        if (other$patientName != null) {
                            return false;
                        }
                    } else if (!this$patientName.equals(other$patientName)) {
                        return false;
                    }

                    label122: {
                        Object this$patientGender = this.getPatientGender();
                        Object other$patientGender = other.getPatientGender();
                        if (this$patientGender == null) {
                            if (other$patientGender == null) {
                                break label122;
                            }
                        } else if (this$patientGender.equals(other$patientGender)) {
                            break label122;
                        }

                        return false;
                    }

                    label115: {
                        Object this$admType = this.getAdmType();
                        Object other$admType = other.getAdmType();
                        if (this$admType == null) {
                            if (other$admType == null) {
                                break label115;
                            }
                        } else if (this$admType.equals(other$admType)) {
                            break label115;
                        }

                        return false;
                    }

                    Object this$admDate = this.getAdmDate();
                    Object other$admDate = other.getAdmDate();
                    if (this$admDate == null) {
                        if (other$admDate != null) {
                            return false;
                        }
                    } else if (!this$admDate.equals(other$admDate)) {
                        return false;
                    }

                    Object this$admDept = this.getAdmDept();
                    Object other$admDept = other.getAdmDept();
                    if (this$admDept == null) {
                        if (other$admDept != null) {
                            return false;
                        }
                    } else if (!this$admDept.equals(other$admDept)) {
                        return false;
                    }

                    label94: {
                        Object this$dishDate = this.getDishDate();
                        Object other$dishDate = other.getDishDate();
                        if (this$dishDate == null) {
                            if (other$dishDate == null) {
                                break label94;
                            }
                        } else if (this$dishDate.equals(other$dishDate)) {
                            break label94;
                        }

                        return false;
                    }

                    label87: {
                        Object this$dishDept = this.getDishDept();
                        Object other$dishDept = other.getDishDept();
                        if (this$dishDept == null) {
                            if (other$dishDept == null) {
                                break label87;
                            }
                        } else if (this$dishDept.equals(other$dishDept)) {
                            break label87;
                        }

                        return false;
                    }

                    Object this$diagName = this.getDiagName();
                    Object other$diagName = other.getDiagName();
                    if (this$diagName == null) {
                        if (other$diagName != null) {
                            return false;
                        }
                    } else if (!this$diagName.equals(other$diagName)) {
                        return false;
                    }

                    Object this$highLight = this.getHighLight();
                    Object other$highLight = other.getHighLight();
                    if (this$highLight == null) {
                        if (other$highLight != null) {
                            return false;
                        }
                    } else if (!this$highLight.equals(other$highLight)) {
                        return false;
                    }

                    return true;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof FulltextSearchResult.Result;
        }

        public int hashCode() {
            int result = 1;
            Object $regNo = this.getRegNo();
            result = result * 59 + ($regNo == null ? 43 : $regNo.hashCode());
            Object $admNo = this.getAdmNo();
            result = result * 59 + ($admNo == null ? 43 : $admNo.hashCode());
            Object $patientName = this.getPatientName();
            result = result * 59 + ($patientName == null ? 43 : $patientName.hashCode());
            Object $patientGender = this.getPatientGender();
            result = result * 59 + ($patientGender == null ? 43 : $patientGender.hashCode());
            Object $admType = this.getAdmType();
            result = result * 59 + ($admType == null ? 43 : $admType.hashCode());
            Object $admDate = this.getAdmDate();
            result = result * 59 + ($admDate == null ? 43 : $admDate.hashCode());
            Object $admDept = this.getAdmDept();
            result = result * 59 + ($admDept == null ? 43 : $admDept.hashCode());
            Object $dishDate = this.getDishDate();
            result = result * 59 + ($dishDate == null ? 43 : $dishDate.hashCode());
            Object $dishDept = this.getDishDept();
            result = result * 59 + ($dishDept == null ? 43 : $dishDept.hashCode());
            Object $diagName = this.getDiagName();
            result = result * 59 + ($diagName == null ? 43 : $diagName.hashCode());
            Object $highLight = this.getHighLight();
            result = result * 59 + ($highLight == null ? 43 : $highLight.hashCode());
            return result;
        }

        public String toString() {
            return "FulltextSearchResult.Result(regNo=" + this.getRegNo() + ", admNo=" + this.getAdmNo() + ", patientName=" + this.getPatientName() + ", patientGender=" + this.getPatientGender() + ", admType=" + this.getAdmType() + ", admDate=" + this.getAdmDate() + ", admDept=" + this.getAdmDept() + ", dishDate=" + this.getDishDate() + ", dishDept=" + this.getDishDept() + ", diagName=" + this.getDiagName() + ", highLight=" + this.getHighLight() + ")";
        }
    }
}
