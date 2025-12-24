-- Seed initial activities

SET time_zone = '+00:00';
SET @now := UTC_TIMESTAMP(3);

INSERT INTO activity (id, name, active, description, created_at, updated_at) VALUES
(1, 'Others', 1, 'Non-ticket/non-change work (admin, internal meetings, training, internal improvements) not attributable to Ticket, MIC, Problem or MAC project entries.', @now, @now),
(2, 'Ticket', 1, 'Work linked to a service ticket (incident/request). Includes analysis, fix, test, user communication and documentation, limited to the ticket scope.', @now, @now),
(3, 'Problem', 1, 'Recurring/systemic issue work focused on root-cause analysis and permanent prevention, often spanning multiple tickets and including corrective actions.', @now, @now),
(4, 'MIC Change', 1, 'Small, controlled change outside projects (minor config/dev/report). Includes brief concept, implementation, testing and deployment with change documentation.', @now, @now);