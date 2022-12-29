
    create table appeal (
       id bigint not null auto_increment,
        created datetime(6),
        updated datetime(6),
        version integer,
        appealed_by_id bigint,
        judicial_authorisation_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table appeal_extra_parameters (
       appeal_id bigint not null,
        extra_parameter_value varchar(255),
        extra_parameter_key varchar(255) not null,
        primary key (appeal_id, extra_parameter_key)
    ) engine=InnoDB;

    create table attachment (
       id bigint not null auto_increment,
        created datetime(6),
        updated datetime(6),
        version integer,
        category varchar(255),
        extension varchar(255),
        file longtext,
        mime_type varchar(255),
        name varchar(255),
        note varchar(1000),
        errand_id bigint,
        decision_id bigint,
        appeal_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table attachment_extra_parameters (
       attachment_id bigint not null,
        extra_parameter_value varchar(255),
        extra_parameter_key varchar(255) not null,
        primary key (attachment_id, extra_parameter_key)
    ) engine=InnoDB;

    create table decision (
       id bigint not null auto_increment,
        created datetime(6),
        updated datetime(6),
        version integer,
        decided_at datetime(6),
        decision_outcome varchar(255),
        decision_type varchar(255),
        description varchar(1000),
        valid_from datetime(6),
        valid_to datetime(6),
        appeal_id bigint,
        decided_by_id bigint,
        errand_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table decision_extra_parameters (
       decision_id bigint not null,
        extra_parameter_value varchar(255),
        extra_parameter_key varchar(255) not null,
        primary key (decision_id, extra_parameter_key)
    ) engine=InnoDB;

    create table decision_laws (
       decision_id bigint not null,
        article varchar(255),
        chapter varchar(255),
        heading varchar(255),
        sfs varchar(255),
        law_order integer not null,
        primary key (decision_id, law_order)
    ) engine=InnoDB;

    create table errand (
       id bigint not null auto_increment,
        created datetime(6),
        updated datetime(6),
        version integer,
        application_received datetime(6),
        case_title_addition varchar(255),
        case_type varchar(255),
        created_by varchar(36),
        created_by_client varchar(255),
        description varchar(255),
        diary_number varchar(255),
        end_date date,
        errand_number varchar(255) not null,
        external_case_id varchar(255),
        municipality_id varchar(255),
        phase varchar(255),
        priority varchar(255),
        process_id varchar(255),
        start_date date,
        updated_by varchar(36),
        updated_by_client varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table errand_extra_parameters (
       errand_id bigint not null,
        extra_parameter_value varchar(255),
        extra_parameter_key varchar(255) not null,
        primary key (errand_id, extra_parameter_key)
    ) engine=InnoDB;

    create table errand_message_ids (
       errand_id bigint not null,
        message_ids varchar(255)
    ) engine=InnoDB;

    create table errand_statuses (
       errand_id bigint not null,
        date_time datetime(6),
        description varchar(255),
        status_type varchar(255),
        status_order integer not null,
        primary key (errand_id, status_order)
    ) engine=InnoDB;

    create table facility (
       id bigint not null auto_increment,
        created datetime(6),
        updated datetime(6),
        version integer,
        address_category varchar(255),
        apartment_number varchar(255),
        attention varchar(255),
        care_of varchar(255),
        city varchar(255),
        country varchar(255),
        house_number varchar(255),
        invoice_marking varchar(255),
        is_zoning_plan_area bit,
        latitude double precision,
        longitude double precision,
        postal_code varchar(255),
        property_designation varchar(255),
        street varchar(255),
        description varchar(255),
        facility_collection_name varchar(255),
        facility_type integer,
        main_facility bit,
        errand_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table facility_extra_parameters (
       facility_id bigint not null,
        extra_parameter_value varchar(255),
        extra_parameter_key varchar(255) not null,
        primary key (facility_id, extra_parameter_key)
    ) engine=InnoDB;

    create table note (
       id bigint not null auto_increment,
        created datetime(6),
        updated datetime(6),
        version integer,
        created_by varchar(36),
        text varchar(10000),
        title varchar(255),
        updated_by varchar(36),
        errand_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table note_extra_parameters (
       note_id bigint not null,
        extra_parameter_value varchar(255),
        extra_parameter_key varchar(255) not null,
        primary key (note_id, extra_parameter_key)
    ) engine=InnoDB;

    create table stakeholder (
       id bigint not null auto_increment,
        created datetime(6),
        updated datetime(6),
        version integer,
        ad_account varchar(255),
        authorized_signatory varchar(255),
        first_name varchar(255),
        last_name varchar(255),
        organization_name varchar(255),
        organization_number varchar(255),
        person_id varchar(255),
        type varchar(255),
        errand_id bigint,
        primary key (id)
    ) engine=InnoDB;

    create table stakeholder_addresses (
       stakeholder_id bigint not null,
        address_category varchar(255),
        apartment_number varchar(255),
        attention varchar(255),
        care_of varchar(255),
        city varchar(255),
        country varchar(255),
        house_number varchar(255),
        invoice_marking varchar(255),
        is_zoning_plan_area bit,
        latitude double precision,
        longitude double precision,
        postal_code varchar(255),
        property_designation varchar(255),
        street varchar(255),
        address_order integer not null,
        primary key (stakeholder_id, address_order)
    ) engine=InnoDB;

    create table stakeholder_contact_information (
       stakeholder_id bigint not null,
        contact_type varchar(255),
        value varchar(255),
        contact_information_order integer not null,
        primary key (stakeholder_id, contact_information_order)
    ) engine=InnoDB;

    create table stakeholder_extra_parameters (
       stakeholder_id bigint not null,
        extra_parameter_value varchar(255),
        extra_parameter_key varchar(255) not null,
        primary key (stakeholder_id, extra_parameter_key)
    ) engine=InnoDB;

    create table stakeholder_roles (
       stakeholder_id bigint not null,
        roles varchar(255),
        role_order integer not null,
        primary key (stakeholder_id, role_order)
    ) engine=InnoDB;

    alter table errand 
       add constraint UK_errand_errand_number unique (errand_number);

    alter table appeal 
       add constraint FK_appeal_appealed_by_id 
       foreign key (appealed_by_id) 
       references stakeholder (id);

    alter table appeal 
       add constraint FK_appeal_judicial_authorisation_id 
       foreign key (judicial_authorisation_id) 
       references stakeholder (id);

    alter table appeal_extra_parameters 
       add constraint FK_appeal_extra_parameters_appeal_id 
       foreign key (appeal_id) 
       references appeal (id);

    alter table attachment 
       add constraint FK_attachment_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table attachment 
       add constraint FK_decision_id 
       foreign key (decision_id) 
       references decision (id);

    alter table attachment 
       add constraint FK_appeal_id 
       foreign key (appeal_id) 
       references appeal (id);

    alter table attachment_extra_parameters 
       add constraint FK_attachment_extra_parameters_attachment_id 
       foreign key (attachment_id) 
       references attachment (id);

    alter table decision 
       add constraint FK_decision_appeal_id 
       foreign key (appeal_id) 
       references appeal (id);

    alter table decision 
       add constraint FK_decision_decided_by_id 
       foreign key (decided_by_id) 
       references stakeholder (id);

    alter table decision 
       add constraint FK_decision_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table decision_extra_parameters 
       add constraint FK_decision_extra_parameters_decision_id 
       foreign key (decision_id) 
       references decision (id);

    alter table decision_laws 
       add constraint FK_decision_laws_decision_id 
       foreign key (decision_id) 
       references decision (id);

    alter table errand_extra_parameters 
       add constraint FK_errand_extra_parameters_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table errand_message_ids 
       add constraint FK_errand_message_ids_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table errand_statuses 
       add constraint FK_errand_statuses_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table facility 
       add constraint FK_facility_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table facility_extra_parameters 
       add constraint FK_facility_extra_parameters_facility_id 
       foreign key (facility_id) 
       references facility (id);

    alter table note 
       add constraint FK_note_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table note_extra_parameters 
       add constraint FK_note_extra_parameters_note_id 
       foreign key (note_id) 
       references note (id);

    alter table stakeholder 
       add constraint FK_stakeholder_errand_id 
       foreign key (errand_id) 
       references errand (id);

    alter table stakeholder_addresses 
       add constraint FK_stakeholder_addresses_stakeholder_id 
       foreign key (stakeholder_id) 
       references stakeholder (id);

    alter table stakeholder_contact_information 
       add constraint FK_stakeholder_contact_information_stakeholder_id 
       foreign key (stakeholder_id) 
       references stakeholder (id);

    alter table stakeholder_extra_parameters 
       add constraint FK_stakeholder_extra_parameters_stakeholder_id 
       foreign key (stakeholder_id) 
       references stakeholder (id);

    alter table stakeholder_roles 
       add constraint FK_stakeholder_roles_stakeholder_id 
       foreign key (stakeholder_id) 
       references stakeholder (id);
