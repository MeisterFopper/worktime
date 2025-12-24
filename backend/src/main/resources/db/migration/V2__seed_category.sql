-- Seed initial categories

SET time_zone = '+00:00';
SET @now := UTC_TIMESTAMP(3);

INSERT INTO category (id, name, active, description, created_at, updated_at) VALUES
(1,  'Intake & Triage', 1, 'Capture requests, clarify needs, categorize and prioritize, route to owners, define next actions, and ensure complete initial information.', @now, @now),
(2,  'Analysis & Diagnostics', 1, 'Reproduce issues, analyze logs/data, identify root cause, assess business/technical impact, define resolution options.', @now, @now),
(3,  'Solution Design & Conception', 1, 'Define solution approach and architecture, produce functional/technical concept, evaluate options/risks, and confirm alignment with requirements.', @now, @now),
(4,  'Implementation & Build', 1, 'Configure ERP settings, develop enhancements/integrations/reports, implement workflows/authorizations, and deliver build artifacts per design.', @now, @now),
(5,  'Debugging & Fixing', 1, 'Implement bug fixes/hotfixes, correct logic/config, prevent regressions, validate resolution, prepare patches and technical notes.', @now, @now),
(6,  'Testing & Quality Assurance', 1, 'Plan or execute tests (unit, integration, UAT support), create test cases, manage defects, confirm acceptance criteria, and support sign-off.', @now, @now),
(7,  'Deployment & Release Management', 1, 'Plan and execute transports/migrations, manage release scope, cutover and rollback plans, coordinate approvals and go-live steps.', @now, @now),
(8,  'Documentation', 1, 'Create/update specs, user guides, runbooks, change logs, configuration records, and operational procedures to ensure traceability and reuse.', @now, @now),
(9,  'Communication & Stakeholder Management', 1, 'Provide status updates, coordinate parties, manage expectations, align scope/priorities, escalate and document decisions.', @now, @now),
(10, 'Knowledge Transfer & Training', 1, 'Enable users and support teams via training, handover sessions, coaching, and knowledge base updates; ensure operational readiness.', @now, @now),
(11, 'Operations & Monitoring', 1, 'Monitor jobs/interfaces/system health, handle alerts, troubleshoot run issues, perform routine checks, and coordinate incident prevention measures.', @now, @now),
(12, 'Continuous Improvement', 1, 'Optimize processes and configurations, reduce recurring effort, improve templates/standards, automate steps, groom backlog and prioritize improvements.', @now, @now);
